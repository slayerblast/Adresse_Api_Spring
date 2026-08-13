package fr.natsystem.projet.batch.step;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;


@Slf4j
@Component
@RequiredArgsConstructor
public class CsvToStagingTasklet implements Tasklet {
    @Value("${spring.batch.pathFile}")
    private String pathFile;
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    @Override
    public RepeatStatus execute(
            StepContribution contribution,
            ChunkContext chunkContext) throws SQLException {
        File folder = new File(pathFile);
        File[] files = folder.listFiles(File::isFile);

        String csvPath = files[0].getAbsolutePath();


        try (Connection connection = dataSource.getConnection()) {


            log.info("Import du fichier {}", csvPath);

            PGConnection pgConnection =
                    connection.unwrap(PGConnection.class);

            CopyManager copyManager =
                    pgConnection.getCopyAPI();

            try (Reader reader =
                         Files.newBufferedReader(Path.of(csvPath))) {

                long nbRows = copyManager.copyIn(
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

                log.info("{} lignes importées dans adresse_staging",
                        nbRows);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return RepeatStatus.FINISHED;
    }
}