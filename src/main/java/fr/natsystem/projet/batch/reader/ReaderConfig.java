package fr.natsystem.projet.batch.reader;

import fr.natsystem.projet.batch.mapper.AdresseFieldSetMapper;
import fr.natsystem.projet.batch.mapper.AdresseRowMapper;
import fr.natsystem.projet.batch.mapper.DvfRowMapper;
import fr.natsystem.projet.model.Adresse;
import fr.natsystem.projet.model.Dvf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.database.JdbcPagingItemReader;
import org.springframework.batch.infrastructure.item.database.Order;
import org.springframework.batch.infrastructure.item.database.support.SqlitePagingQueryProvider;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import javax.sql.DataSource;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Configuration
public class ReaderConfig {
    @Value("${spring.batch.pathFile}")
    private String pathFile;

    @Bean
    @StepScope
    public JdbcPagingItemReader<Adresse> stagingReader(
            DataSource ds,
            @Value("#{stepExecutionContext['codeInsee']}")
            String codeInsee)
            throws Exception {

        SqlitePagingQueryProvider provider =new SqlitePagingQueryProvider();

        provider.setSelectClause("""
            SELECT
                id, id_fantoir, numero, rep,nom_voie, code_postal,
                code_insee,nom_commune,code_insee_ancienne_commune,
                nom_ancienne_commune, x, y, lon, lat, type_position,
                alias, nom_ld, libelle_acheminement, nom_afnor,
                source_position, source_nom_voie,certification_commune,
                cad_parcelles
            """);

        provider.setFromClause("FROM adresse_staging ");

        provider.setWhereClause("WHERE code_insee = :codeInsee ");

        Map<String, Order> sortKeys = new LinkedHashMap<>();
        sortKeys.put( "id",Order.ASCENDING);
        provider.setSortKeys(sortKeys);
        JdbcPagingItemReader<Adresse> reader = new JdbcPagingItemReader<>(ds, provider);


        reader.setDataSource(ds);
        reader.setQueryProvider(provider);
        reader.setParameterValues(Map.of("codeInsee", codeInsee));
        reader.setPageSize(10000);
        reader.setRowMapper(new AdresseRowMapper());
        reader.afterPropertiesSet();

        return reader;
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<Dvf> stagingReaderDvf(
            DataSource ds,
            @Value("#{stepExecutionContext['codeInsee']}")
            String codeInsee)
            throws Exception {

        SqlitePagingQueryProvider provider =new SqlitePagingQueryProvider();

        provider.setSelectClause("""
                SELECT
                    id,id_mutation,date_mutation,numero_disposition,
                    nature_mutation,valeur_fonciere,adresse_numero,
                    adresse_suffixe,adresse_code_voie, adresse_nom_voie,
                    code_postal,code_commune,nom_commune,ancien_code_commune,
                    ancien_nom_commune,code_departement,id_parcelle,ancien_id_parcelle,
                    numero_volume,lot_1_numero,lot_1_surface_carrez,lot_2_numero,
                    lot_2_surface_carrez,lot_3_numero,lot_3_surface_carrez,
                    lot_4_numero,lot_4_surface_carrez,lot_5_numero,lot_5_surface_carrez,
                    nombre_lots,code_type_local,type_local,surface_reelle_bati,
                    nombre_pieces_principales,code_nature_culture,nature_culture,
                    code_nature_culture_speciale,nature_culture_speciale,
                    surface_terrain,longitude,latitude
            """);

        provider.setFromClause("FROM dvf_staging ");

        provider.setWhereClause("WHERE code_commune = :codeInsee ");

        Map<String, Order> sortKeys = new LinkedHashMap<>();
        sortKeys.put( "id",Order.ASCENDING);
        provider.setSortKeys(sortKeys);
        JdbcPagingItemReader<Dvf> reader = new JdbcPagingItemReader<>(ds, provider);

        reader.setDataSource(ds);
        reader.setQueryProvider(provider);
        reader.setParameterValues(Map.of("codeInsee", codeInsee));
        reader.setPageSize(10000);
        reader.setRowMapper(new DvfRowMapper());
        reader.afterPropertiesSet();

        return reader;
    }
}
