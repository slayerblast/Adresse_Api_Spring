package fr.natsystem.projet.Repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.natsystem.projet.batch.mapper.AdresseRowMapper;
import fr.natsystem.projet.model.Adresse;
import fr.natsystem.projet.repository.AdresseRepositoryPostgres;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@ExtendWith(MockitoExtension.class)
class AdresseRepositoryPostgresTest {

  private static final int SEARCH_PAGE = 2;
  private static final int PAGE_SIZE = 20;
  private static final long EXPECTED_TOTAL_ELEMENTS = 42L;
  private static final int EXPECTED_TOTAL_PAGES = 3;
  private static final long EXPECTED_OFFSET = 40L;

  private static final int DEFAULT_PAGE_SIZE = 10;
  private static final int NEAREST_ADDRESS_PARAMETERS_COUNT = 4;
  private static final int LAST_PARAMETER_INDEX = 3;
  private static final double X_COORDINATE = 651_000.0;
  private static final double Y_COORDINATE = 6_862_000.0;
  private static final double LONGITUDE = 2.3522;
  private static final double LATITUDE = 48.8566;

  @Mock private AdresseRowMapper adresseRowMapper;

  @Mock private JdbcTemplate jdbcTemplate;

  private AdresseRepositoryPostgres repository;

  @BeforeEach
  void setUp() {
    repository = new AdresseRepositoryPostgres(adresseRowMapper, jdbcTemplate);
  }

  @Test
  void shouldSearchWithAllFilters() {
    Pageable pageable = PageRequest.of(SEARCH_PAGE, PAGE_SIZE);

    Adresse firstAddress = createAdresse("adresse-001", "75001", "Paris");

    Adresse secondAddress = createAdresse("adresse-002", "75001", "Paris");

    List<Adresse> expectedAddresses = List.of(firstAddress, secondAddress);

    when(jdbcTemplate.query(anyString(), eq(adresseRowMapper), any(Object[].class)))
        .thenReturn(expectedAddresses);

    when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(Object[].class)))
        .thenReturn(EXPECTED_TOTAL_ELEMENTS);

    Page<Adresse> result = repository.rechercher("750", "rue de", "paris", pageable);

    assertAll(
        "Pagination",
        () -> assertEquals(expectedAddresses, result.getContent()),
        () -> assertEquals(EXPECTED_TOTAL_ELEMENTS, result.getTotalElements()),
        () -> assertEquals(EXPECTED_TOTAL_PAGES, result.getTotalPages()),
        () -> assertEquals(SEARCH_PAGE, result.getNumber()),
        () -> assertEquals(PAGE_SIZE, result.getSize()));

    ArgumentCaptor<String> searchSqlCaptor = ArgumentCaptor.forClass(String.class);

    ArgumentCaptor<Object[]> searchParametersCaptor = ArgumentCaptor.forClass(Object[].class);

    verify(jdbcTemplate)
        .query(searchSqlCaptor.capture(), eq(adresseRowMapper), searchParametersCaptor.capture());

    String searchSql = normalize(searchSqlCaptor.getValue());

    Object[] searchParameters = searchParametersCaptor.getValue();

    assertAll(
        "Requête de recherche",
        () -> assertTrue(searchSql.contains("SELECT * FROM adresse")),
        () -> assertTrue(searchSql.contains("code_postal LIKE ?")),
        () -> assertTrue(searchSql.contains("LOWER(nom_voie) LIKE ?")),
        () -> assertTrue(searchSql.contains("LOWER(nom_commune) LIKE ?")),
        () -> assertTrue(searchSql.contains("ORDER BY id")),
        () -> assertTrue(searchSql.contains("LIMIT ? OFFSET ?")),
        () ->
            assertArrayEquals(
                new Object[] {"750%", "rue de%", "paris%", PAGE_SIZE, EXPECTED_OFFSET},
                searchParameters));

    ArgumentCaptor<String> countSqlCaptor = ArgumentCaptor.forClass(String.class);

    ArgumentCaptor<Object[]> countParametersCaptor = ArgumentCaptor.forClass(Object[].class);

    verify(jdbcTemplate)
        .queryForObject(countSqlCaptor.capture(), eq(Long.class), countParametersCaptor.capture());

    String countSql = normalize(countSqlCaptor.getValue());

    Object[] countParameters = countParametersCaptor.getValue();

    assertAll(
        "Requête de comptage",
        () -> assertTrue(countSql.contains("SELECT COUNT(*) FROM adresse")),
        () -> assertTrue(countSql.contains("code_postal LIKE ?")),
        () -> assertTrue(countSql.contains("LOWER(nom_voie) LIKE ?")),
        () -> assertTrue(countSql.contains("LOWER(nom_commune) LIKE ?")),
        () -> assertArrayEquals(new Object[] {"750%", "rue de%", "paris%"}, countParameters));
  }

  @Test
  void shouldSearchWithoutFilters() {
    Pageable pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE);

    when(jdbcTemplate.query(anyString(), eq(adresseRowMapper), any(Object[].class)))
        .thenReturn(List.of());

    when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(Object[].class)))
        .thenReturn(0L);

    Page<Adresse> result = repository.rechercher(null, "", "   ", pageable);

    assertAll(
        () -> assertTrue(result.isEmpty()),
        () -> assertEquals(0L, result.getTotalElements()),
        () -> assertEquals(0, result.getNumber()),
        () -> assertEquals(DEFAULT_PAGE_SIZE, result.getSize()));

    ArgumentCaptor<String> searchSqlCaptor = ArgumentCaptor.forClass(String.class);

    ArgumentCaptor<Object[]> searchParametersCaptor = ArgumentCaptor.forClass(Object[].class);

    verify(jdbcTemplate)
        .query(searchSqlCaptor.capture(), eq(adresseRowMapper), searchParametersCaptor.capture());

    String searchSql = normalize(searchSqlCaptor.getValue());

    Object[] searchParameters = searchParametersCaptor.getValue();

    assertAll(
        () -> assertTrue(searchSql.contains("WHERE 1=1")),
        () -> assertTrue(searchSql.contains("ORDER BY id")),
        () -> assertEquals(2, searchParameters.length),
        () -> assertEquals(DEFAULT_PAGE_SIZE, searchParameters[0]),
        () -> assertEquals(0L, searchParameters[1]));

    ArgumentCaptor<Object[]> countParametersCaptor = ArgumentCaptor.forClass(Object[].class);

    verify(jdbcTemplate)
        .queryForObject(anyString(), eq(Long.class), countParametersCaptor.capture());

    assertEquals(0, countParametersCaptor.getValue().length);
  }

  @Test
  void shouldUseZeroWhenCountQueryReturnsNull() {
    Pageable pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE);

    when(jdbcTemplate.query(anyString(), eq(adresseRowMapper), any(Object[].class)))
        .thenReturn(List.of());

    when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(Object[].class)))
        .thenReturn(null);

    Page<Adresse> result = repository.rechercher(null, null, null, pageable);

    assertEquals(0L, result.getTotalElements());
  }

  @Test
  void shouldNormalizeQueryForAutoComplete() {
    Adresse expectedAddress = createAdresse("adresse-001", "75001", "Paris");

    when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Object[].class)))
        .thenReturn(List.of(expectedAddress));

    List<Adresse> result = repository.autoComplete("  Rue de l'Église-Saint-Paul  ");

    assertAll(
        () -> assertEquals(1, result.size()), () -> assertSame(expectedAddress, result.getFirst()));

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);

    ArgumentCaptor<Object[]> parametersCaptor = ArgumentCaptor.forClass(Object[].class);

    verify(jdbcTemplate)
        .query(sqlCaptor.capture(), any(AdresseRowMapper.class), parametersCaptor.capture());

    String sql = normalize(sqlCaptor.getValue());
    Object[] parameters = parametersCaptor.getValue();

    assertAll(
        () -> assertTrue(sql.contains("SELECT")),
        () -> assertTrue(sql.contains("word_similarity(?, search_text) AS score")),
        () -> assertTrue(sql.contains("FROM adresse")),
        () -> assertTrue(sql.contains("WHERE ? <% search_text")),
        () -> assertTrue(sql.contains("ORDER BY score DESC")),
        () -> assertTrue(sql.contains("LIMIT 10")),
        () -> assertEquals(2, parameters.length),
        () -> assertEquals("rue de l eglise saint paul", parameters[0]),
        () -> assertEquals("rue de l eglise saint paul", parameters[1]));
  }

  @Test
  void shouldFindNearestAddress() {
    double latitude = LATITUDE;
    double longitude = LONGITUDE;

    Adresse expectedAddress = createAdresse("adresse-001", "75001", "Paris");

    when(jdbcTemplate.query(anyString(), eq(adresseRowMapper), any(Object[].class)))
        .thenReturn(List.of(expectedAddress));

    List<Adresse> result = repository.findProches(latitude, longitude);

    assertAll(
        () -> assertEquals(1, result.size()), () -> assertSame(expectedAddress, result.getFirst()));

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Object[]> parametersCaptor = ArgumentCaptor.forClass(Object[].class);

    verify(jdbcTemplate)
        .query(sqlCaptor.capture(), eq(adresseRowMapper), parametersCaptor.capture());

    String sql = normalize(sqlCaptor.getValue());
    Object[] parameters = parametersCaptor.getValue();

    assertAll(
        () -> assertTrue(sql.contains("ST_Distance")),
        () -> assertFalse(sql.contains("ST_DWithin")),
        () -> assertTrue(sql.contains("ST_MakePoint(?, ?)")),
        () -> assertTrue(sql.contains("<->")),
        () -> assertTrue(sql.contains("LIMIT 1")),
        () -> assertEquals(NEAREST_ADDRESS_PARAMETERS_COUNT, parameters.length),
        () -> assertEquals(longitude, parameters[0]),
        () -> assertEquals(latitude, parameters[1]),
        () -> assertEquals(longitude, parameters[2]),
        () -> assertEquals(latitude, parameters[LAST_PARAMETER_INDEX]));
  }

  private Adresse createAdresse(String id, String codePostal, String commune) {
    return new Adresse(
        id,
        "FANTOIR-001",
        "10",
        "",
        "Rue de Paris",
        codePostal,
        "75056",
        commune,
        "",
        "",
        X_COORDINATE,
        Y_COORDINATE,
        LONGITUDE,
        LATITUDE,
        "entrée",
        "",
        "",
        commune.toUpperCase(),
        "RUE DE PARIS",
        "BAN",
        "BAN",
        1,
        "");
  }

  private String normalize(String sql) {
    return sql.replaceAll("\\s+", " ").trim();
  }
}
