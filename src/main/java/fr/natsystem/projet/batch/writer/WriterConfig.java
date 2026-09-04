package fr.natsystem.projet.batch.writer;

import fr.natsystem.projet.model.Adresse;
import fr.natsystem.projet.model.Dvf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class WriterConfig {


    @Bean(name = "jdbcWriter")
    @Profile("postgres")
    public JdbcBatchItemWriter<Adresse> jdbcPostgresWriter(
            DataSource ds) {
        return new JdbcBatchItemWriterBuilder<Adresse>()
                .dataSource(ds)
                .sql("""   
                        INSERT INTO adresse (
                            id, id_fantoir, numero, rep, nom_voie, code_postal, code_insee,
                            nom_commune, code_insee_ancienne_commune, nom_ancienne_commune,
                            x, y, lon, lat, type_position, alias, nom_ld,
                            libelle_acheminement, nom_afnor, source_position, source_nom_voie,
                            certification_commune, cad_parcelles, search_text
                        ) VALUES (
                            :id, :id_fantoir, :numero, :rep, :nom_voie, :code_postal, :code_insee,
                            :nom_commune, :code_insee_ancienne_commune, :nom_ancienne_commune,
                            :x, :y, :lon, :lat, :type_position, :alias, :nom_ld,
                            :libelle_acheminement, :nom_afnor, :source_position, :source_nom_voie,
                            :certification_commune, :cad_parcelles, 
                            lower(
                                  unaccent(
                                           concat_ws(
                                                      ' ',
                                                      :numero,
                                                      :nom_voie,
                                                      :code_postal,
                                                       :nom_commune
                                                       )
                                           )
                                  )
                        )
                        ON CONFLICT(id, type_position, x, y)
                        DO UPDATE SET
                            id_fantoir = excluded.id_fantoir,
                            numero = excluded.numero,
                            rep = excluded.rep,
                            nom_voie = excluded.nom_voie,
                            code_postal = excluded.code_postal,
                            code_insee = excluded.code_insee,
                            nom_commune = excluded.nom_commune,
                            code_insee_ancienne_commune = excluded.code_insee_ancienne_commune,
                            nom_ancienne_commune = excluded.nom_ancienne_commune,
                            lon = excluded.lon,
                            lat = excluded.lat,
                            alias = excluded.alias,
                            nom_ld = excluded.nom_ld,
                            libelle_acheminement = excluded.libelle_acheminement,
                            nom_afnor = excluded.nom_afnor,
                            source_position = excluded.source_position,
                            source_nom_voie = excluded.source_nom_voie,
                            certification_commune = excluded.certification_commune,
                            cad_parcelles = excluded.cad_parcelles ;
                       
                        """) // :paramName -> getter du bean
                .beanMapped()
                .assertUpdates(true)
                .build();
    }

    @Bean(name = "jdbcWriterDvf")
    @Profile("postgres")
    public JdbcBatchItemWriter<Dvf> jdbcPostgresWriterDvf(
            DataSource ds) {
        return new JdbcBatchItemWriterBuilder<Dvf>()
                .dataSource(ds)
                .sql("""   
                        INSERT INTO dvf (
                            id_mutation, date_mutation, numero_disposition, nature_mutation, valeur_fonciere,
                            adresse_numero, adresse_suffixe, adresse_code_voie, adresse_nom_voie,
                            code_postal, code_commune, nom_commune, ancien_code_commune,
                            ancien_nom_commune, code_departement, id_parcelle, ancien_id_parcelle, numero_volume,
                            lot_1_numero, lot_1_surface_carrez, lot_2_numero, lot_2_surface_carrez,
                            lot_3_numero, lot_3_surface_carrez, lot_4_numero, lot_4_surface_carrez,
                            lot_5_numero, lot_5_surface_carrez, nombre_lots, code_type_local, type_local,
                            surface_reelle_bati, nombre_pieces_principales, code_nature_culture,
                            nature_culture, code_nature_culture_speciale, nature_culture_speciale,
                            surface_terrain, longitude, latitude
                        ) VALUES (
                            :id_mutation, :date_mutation, :numero_disposition, :nature_mutation, :valeur_fonciere,
                            :adresse_numero, :adresse_suffixe, :adresse_code_voie, :adresse_nom_voie,
                            :code_postal, :code_commune, :nom_commune, :ancien_code_commune,
                            :ancien_nom_commune, :code_departement, :id_parcelle, :ancien_id_parcelle, :numero_volume,
                            :lot_1_numero, :lot_1_surface_carrez, :lot_2_numero, :lot_2_surface_carrez,
                            :lot_3_numero, :lot_3_surface_carrez, :lot_4_numero, :lot_4_surface_carrez,
                            :lot_5_numero, :lot_5_surface_carrez, :nombre_lots, :code_type_local, :type_local,
                            :surface_reelle_bati, :nombre_pieces_principales, :code_nature_culture,
                            :nature_culture, :code_nature_culture_speciale, :nature_culture_speciale,
                            :surface_terrain, :longitude, :latitude
                        );
                        """) // :paramName -> getter du bean
                .beanMapped()
                .assertUpdates(true)
                .build();
    }


    @Bean(name = "jdbcWriter")
    @Profile("sqlite")
    public JdbcBatchItemWriter<Adresse> jdbcSqliteWriter(
            DataSource ds) {
        return new JdbcBatchItemWriterBuilder<Adresse>()
                .dataSource(ds)
                .sql("""   
                        INSERT INTO adresse (
                            id, id_fantoir, numero, rep, nom_voie, code_postal, code_insee,
                            nom_commune, code_insee_ancienne_commune, nom_ancienne_commune,
                            x, y, lon, lat, type_position, alias, nom_ld,
                            libelle_acheminement, nom_afnor, source_position, source_nom_voie,
                            certification_commune, cad_parcelles
                        ) VALUES (
                            :id, :id_fantoir, :numero, :rep, :nom_voie, :code_postal, :code_insee,
                            :nom_commune, :code_insee_ancienne_commune, :nom_ancienne_commune,
                            :x, :y, :lon, :lat, :type_position, :alias, :nom_ld,
                            :libelle_acheminement, :nom_afnor, :source_position, :source_nom_voie,
                            :certification_commune, :cad_parcelles
                        )
                        ON CONFLICT(id, type_position, x, y)
                        DO UPDATE SET
                            id_fantoir = excluded.id_fantoir,
                            numero = excluded.numero,
                            rep = excluded.rep,
                            nom_voie = excluded.nom_voie,
                            code_postal = excluded.code_postal,
                            code_insee = excluded.code_insee,
                            nom_commune = excluded.nom_commune,
                            code_insee_ancienne_commune = excluded.code_insee_ancienne_commune,
                            nom_ancienne_commune = excluded.nom_ancienne_commune,
                            lon = excluded.lon,
                            lat = excluded.lat,
                            alias = excluded.alias,
                            nom_ld = excluded.nom_ld,
                            libelle_acheminement = excluded.libelle_acheminement,
                            nom_afnor = excluded.nom_afnor,
                            source_position = excluded.source_position,
                            source_nom_voie = excluded.source_nom_voie,
                            certification_commune = excluded.certification_commune,
                            cad_parcelles = excluded.cad_parcelles ;
                       
                        """) // :paramName -> getter du bean
                .beanMapped()
                .assertUpdates(true)
                .build();
    }

}
