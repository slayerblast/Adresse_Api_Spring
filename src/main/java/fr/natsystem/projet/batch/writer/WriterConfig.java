package fr.natsystem.projet.batch.writer;

import fr.natsystem.projet.model.Adresse;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class WriterConfig {

    @Bean
    public JdbcBatchItemWriter<Adresse> jdbcWriter(
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

    @Bean
    public JdbcBatchItemWriter<Adresse> stagingWriter(
            DataSource ds) {
        return new JdbcBatchItemWriterBuilder<Adresse>()
                .dataSource(ds)
                .sql("""
                INSERT INTO adresse_staging (
                    id, id_fantoir, numero, rep, nom_voie,
                    code_postal, code_insee, nom_commune,
                    code_insee_ancienne_commune,
                    nom_ancienne_commune,
                    x, y, lon, lat,
                    type_position,
                    alias, nom_ld,
                    libelle_acheminement,
                    nom_afnor,
                    source_position,
                    source_nom_voie,
                    certification_commune,
                    cad_parcelles
                ) VALUES (
                    :id, :id_fantoir, :numero, :rep, :nom_voie,
                    :code_postal, :code_insee, :nom_commune,
                    :code_insee_ancienne_commune,
                    :nom_ancienne_commune,
                    :x, :y, :lon, :lat,
                    :type_position,
                    :alias, :nom_ld,
                    :libelle_acheminement,
                    :nom_afnor,
                    :source_position,
                    :source_nom_voie,
                    :certification_commune,
                    :cad_parcelles
                )
            """)
                .beanMapped()
                .build();
    }
}
