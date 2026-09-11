package fr.natsystem.projet.batch.step;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CsvToStagingTasklet implements Tasklet {
  @Value("${spring.batch.pathFile}")
  private String pathFile;

  private final JdbcTemplate jdbcTemplate;
  private final DataSource dataSource;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
      throws SQLException {
    File folder = new File(pathFile);
    File[] files = folder.listFiles(File::isFile);
    String csvPath = files[0].getAbsolutePath();
    String innerJob =
        contribution.getStepExecution().getJobExecution().getJobParameters().getString("innerJob");

    try (Connection connection = dataSource.getConnection()) {

      log.info("Import du fichier {}", csvPath);

      PGConnection pgConnection = connection.unwrap(PGConnection.class);

      CopyManager copyManager = pgConnection.getCopyAPI();

      try (Reader reader = Files.newBufferedReader(Path.of(csvPath))) {
        if (innerJob.equals("importAdresseJob")) {
          long nbRows =
              copyManager.copyIn(
                  """
                            COPY adresse_staging (
                                id,
                                id_fantoir,
                                numero,
                                rep,
                                nom_voie,
                                code_postal,
                                code_insee,
                                nom_commune,
                                code_insee_ancienne_commune,
                                nom_ancienne_commune,
                                x,
                                y,
                                lon,
                                lat,
                                type_position,
                                alias,
                                nom_ld,
                                libelle_acheminement,
                                nom_afnor,
                                source_position,
                                source_nom_voie,
                                certification_commune,
                                cad_parcelles
                            )
                            FROM STDIN
                            WITH (
                                FORMAT CSV,
                                HEADER TRUE,
                                DELIMITER ';'
                            )
                            """,
                  reader);

          log.info("{} lignes importées dans adresse_staging", nbRows);
        } else if (innerJob.equals("importDvfJob")) {
          long nbRows =
              copyManager.copyIn(
                  """
                                    COPY dvf_staging (
                                        id_mutation, date_mutation, numero_disposition, nature_mutation, valeur_fonciere,
                                        adresse_numero, adresse_suffixe, adresse_nom_voie, adresse_code_voie, code_postal,
                                        code_commune, nom_commune, code_departement, ancien_code_commune, ancien_nom_commune,
                                        id_parcelle, ancien_id_parcelle, numero_volume,
                                        lot_1_numero, lot_1_surface_carrez, lot_2_numero, lot_2_surface_carrez,
                                        lot_3_numero, lot_3_surface_carrez, lot_4_numero, lot_4_surface_carrez,
                                        lot_5_numero, lot_5_surface_carrez, nombre_lots,
                                        code_type_local, type_local, surface_reelle_bati, nombre_pieces_principales,
                                        code_nature_culture, nature_culture,
                                        code_nature_culture_speciale, nature_culture_speciale,
                                        surface_terrain, longitude, latitude
                                    )
                                    FROM STDIN
                                    WITH (FORMAT CSV, HEADER TRUE, DELIMITER ',');
                            """,
                  reader);

          log.info("{} lignes importées dans dvf_staging", nbRows);
        } else {
          long nbRows =
              copyManager.copyIn(
                  """
                            COPY adresse_staging (
                                id,
                                id_fantoir,
                                numero,
                                rep,
                                nom_voie,
                                code_postal,
                                code_insee,
                                nom_commune,
                                code_insee_ancienne_commune,
                                nom_ancienne_commune,
                                x,
                                y,
                                lon,
                                lat,
                                type_position,
                                alias,
                                nom_ld,
                                libelle_acheminement,
                                nom_afnor,
                                source_position,
                                source_nom_voie,
                                certification_commune,
                                cad_parcelles
                            )
                            FROM STDIN
                            WITH (
                                FORMAT CSV,
                                HEADER TRUE,
                                DELIMITER ';'
                            )
                            """,
                  reader);

          log.info("{} lignes importées dans adresse_staging", nbRows);
        }

      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    return RepeatStatus.FINISHED;
  }
}
