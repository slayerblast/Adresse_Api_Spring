package fr.natsystem.projet.Reader;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import fr.natsystem.projet.batch.mapper.AdresseRowMapper;
import fr.natsystem.projet.batch.mapper.DvfRowMapper;
import fr.natsystem.projet.batch.reader.ReaderConfig;
import fr.natsystem.projet.model.Adresse;
import fr.natsystem.projet.model.Dvf;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.database.JdbcPagingItemReader;
import org.springframework.batch.infrastructure.item.database.Order;
import org.springframework.batch.infrastructure.item.database.support.SqlitePagingQueryProvider;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.util.ReflectionTestUtils;

class ReaderConfigTest {

  private static final String CODE_INSEE = "94046";
  private static final int PAGESIZE = 10000;
  private ReaderConfig readerConfig;
  private DataSource dataSource;

  @BeforeEach
  void setUp() {
    readerConfig = new ReaderConfig();
    dataSource = mock(DataSource.class);
  }

  @Test
  void shouldConfigureAdresseStagingReader() throws Exception {
    JdbcPagingItemReader<Adresse> reader = readerConfig.stagingReader(dataSource, CODE_INSEE);

    SqlitePagingQueryProvider queryProvider = getQueryProvider(reader);

    Map<String, Object> parameterValues = getParameterValues(reader);

    RowMapper<?> rowMapper = getRowMapper(reader);

    String generatedQuery = normalize(queryProvider.generateFirstPageQuery(PAGESIZE));

    assertAll(
        () -> assertNotNull(reader),
        () -> assertEquals(PAGESIZE, reader.getPageSize()),
        () -> assertEquals(CODE_INSEE, parameterValues.get("codeInsee")),
        () -> assertInstanceOf(AdresseRowMapper.class, rowMapper),
        () -> assertTrue(generatedQuery.contains("SELECT id, id_fantoir, numero")),
        () -> assertTrue(generatedQuery.contains("code_insee")),
        () -> assertTrue(generatedQuery.contains("cad_parcelles")),
        () -> assertTrue(generatedQuery.contains("FROM adresse_staging")),
        () -> assertTrue(generatedQuery.contains("WHERE code_insee = :codeInsee")),
        () -> assertTrue(generatedQuery.contains("ORDER BY id ASC")),
        () -> assertEquals(Order.ASCENDING, queryProvider.getSortKeys().get("id")));
  }

  @Test
  void shouldConfigureDvfStagingReader() throws Exception {
    JdbcPagingItemReader<Dvf> reader = readerConfig.stagingReaderDvf(dataSource, CODE_INSEE);

    SqlitePagingQueryProvider queryProvider = getQueryProvider(reader);

    Map<String, Object> parameterValues = getParameterValues(reader);

    RowMapper<?> rowMapper = getRowMapper(reader);

    String generatedQuery = normalize(queryProvider.generateFirstPageQuery(PAGESIZE));

    assertAll(
        () -> assertNotNull(reader),
        () -> assertEquals(PAGESIZE, reader.getPageSize()),
        () -> assertEquals(CODE_INSEE, parameterValues.get("codeInsee")),
        () -> assertInstanceOf(DvfRowMapper.class, rowMapper),
        () -> assertTrue(generatedQuery.contains("SELECT id,id_mutation,date_mutation")),
        () -> assertTrue(generatedQuery.contains("code_commune")),
        () -> assertTrue(generatedQuery.contains("surface_terrain,longitude,latitude")),
        () -> assertTrue(generatedQuery.contains("FROM dvf_staging")),
        () -> assertTrue(generatedQuery.contains("WHERE code_commune = :codeInsee")),
        () -> assertTrue(generatedQuery.contains("ORDER BY id ASC")),
        () -> assertEquals(Order.ASCENDING, queryProvider.getSortKeys().get("id")));
  }

  @Test
  void shouldUseAdresseRowMapperForAdresseReader() throws Exception {

    JdbcPagingItemReader<Adresse> reader = readerConfig.stagingReader(dataSource, CODE_INSEE);

    RowMapper<?> rowMapper = getRowMapper(reader);

    assertInstanceOf(AdresseRowMapper.class, rowMapper);
  }

  @Test
  void shouldUseDvfRowMapperForDvfReader() throws Exception {

    JdbcPagingItemReader<Dvf> reader = readerConfig.stagingReaderDvf(dataSource, CODE_INSEE);

    RowMapper<?> rowMapper = getRowMapper(reader);

    assertInstanceOf(DvfRowMapper.class, rowMapper);
  }

  private SqlitePagingQueryProvider getQueryProvider(JdbcPagingItemReader<?> reader) {
    Object queryProvider = ReflectionTestUtils.getField(reader, "queryProvider");

    assertNotNull(queryProvider, "Le queryProvider ne doit pas être null");

    return assertInstanceOf(SqlitePagingQueryProvider.class, queryProvider);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> getParameterValues(JdbcPagingItemReader<?> reader) {
    Object parameterValues = ReflectionTestUtils.getField(reader, "parameterValues");

    assertNotNull(parameterValues, "Les paramètres du reader ne doivent pas être null");

    return (Map<String, Object>) parameterValues;
  }

  private RowMapper<?> getRowMapper(JdbcPagingItemReader<?> reader) {
    Object rowMapper = ReflectionTestUtils.getField(reader, "rowMapper");

    assertNotNull(rowMapper, "Le RowMapper ne doit pas être null");

    return assertInstanceOf(RowMapper.class, rowMapper);
  }

  private String normalize(String value) {
    return value.replaceAll("\\s+", " ").trim();
  }
}
