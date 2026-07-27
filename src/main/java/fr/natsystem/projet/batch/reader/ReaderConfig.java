package fr.natsystem.projet.batch.reader;

import fr.natsystem.projet.batch.mapper.AdresseFieldSetMapper;
import fr.natsystem.projet.batch.mapper.AdresseRowMapper;
import fr.natsystem.projet.model.Adresse;
import org.springframework.batch.infrastructure.item.database.JdbcPagingItemReader;
import org.springframework.batch.infrastructure.item.database.Order;
import org.springframework.batch.infrastructure.item.database.support.SqlitePagingQueryProvider;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.mapping.RecordFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ReaderConfig {

    @Bean
    @StepScope
    public FlatFileItemReader<Adresse> csvDynamicReader(
            @Value("#{jobParameters['inputFile']}") String inputFile,
            AdresseFieldSetMapper fieldSetMapper) {

        return new FlatFileItemReaderBuilder<Adresse>()
                .name("csvReader")
                .resource(new FileSystemResource(inputFile))
                .delimited()
                .delimiter(";")
                .names(
                        "id", "id_fantoir", "numero", "rep", "nom_voie",
                        "code_postal", "code_insee", "nom_commune",
                        "code_insee_ancienne_commune", "nom_ancienne_commune",
                        "x", "y", "lon", "lat", "type_position",
                        "alias", "nom_ld", "libelle_acheminement",
                        "nom_afnor", "source_position", "source_nom_voie",
                        "certification_commune", "cad_parcelles"
                )
                .fieldSetMapper(fieldSetMapper)
                .linesToSkip(1)
                .build();
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<Adresse> stagingReader(DataSource ds) throws Exception {

        SqlitePagingQueryProvider provider = new SqlitePagingQueryProvider();
        provider.setSelectClause("""
                SELECT id, id_fantoir, numero, rep, nom_voie, code_postal, code_insee,
                nom_commune, code_insee_ancienne_commune, nom_ancienne_commune,
                x, y, lon, lat, type_position, alias, nom_ld,
                libelle_acheminement, nom_afnor, source_position, source_nom_voie,
                certification_commune, cad_parcelles
                """);
        provider.setFromClause("FROM adresse_staging");

        Map<String, Order> sortKeys = new LinkedHashMap<>();
        sortKeys.put("code_insee", Order.ASCENDING);
        sortKeys.put("id", Order.ASCENDING);
        provider.setSortKeys(sortKeys);

        JdbcPagingItemReader<Adresse> reader =
                new JdbcPagingItemReader<>(ds, provider);

        reader.setName("stagingReader");
        reader.setPageSize(50000);
        reader.setRowMapper(new AdresseRowMapper());
        reader.afterPropertiesSet();

        return reader;
    }
}
