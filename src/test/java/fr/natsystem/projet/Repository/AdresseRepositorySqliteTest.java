package fr.natsystem.projet.Repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.natsystem.projet.batch.mapper.AdresseRowMapper;
import fr.natsystem.projet.model.Adresse;
import fr.natsystem.projet.repository.AdresseRepositorySqlite;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

class AdresseRepositorySqliteTest {

  private static final int DEFAULT_PAGE_SIZE = 20;
  private static final int SEARCH_PAGE_SIZE = 10;
  private static final int PARTIAL_SEARCH_PAGE_SIZE = 5;

  private static final long EXPECTED_TOTAL_ELEMENTS = 21L;
  private static final long EXPECTED_OFFSET = 20L;

  private static final double LATITUDE = 48.8052;
  private static final double LONGITUDE = 2.4385;

  private AdresseRowMapper adresseRowMapper;
  private JdbcTemplate jdbcTemplate;
  private AdresseRepositorySqlite repository;

  @BeforeEach
  void setUp() {
    adresseRowMapper = mock(AdresseRowMapper.class);
    jdbcTemplate = mock(JdbcTemplate.class);

    repository = new AdresseRepositorySqlite(adresseRowMapper, jdbcTemplate);
  }

  @Test
  void shouldSearchWithoutOptionalCriteria() {
    Pageable pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE);

    Adresse adresse1 = mock(Adresse.class);
    Adresse adresse2 = mock(Adresse.class);

    List<Adresse> expectedAdresses = List.of(adresse1, adresse2);

    when(jdbcTemplate.query(anyString(), eq(adresseRowMapper), any(Object[].class)))
        .thenReturn(expectedAdresses);

    when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(Object[].class)))
        .thenReturn(2L);

    Page<Adresse> result = repository.rechercher(null, null, null, pageable);

    assertAll(
        () -> assertThat(result).isNotNull(),
        () -> assertThat(result.getContent()).containsExactly(adresse1, adresse2),
        () -> assertThat(result.getTotalElements()).isEqualTo(2L),
        () -> assertThat(result.getNumber()).isZero(),
        () -> assertThat(result.getSize()).isEqualTo(DEFAULT_PAGE_SIZE));

    ArgumentCaptor<String> searchSqlCaptor = ArgumentCaptor.forClass(String.class);

    ArgumentCaptor<Object[]> searchParametersCaptor = ArgumentCaptor.forClass(Object[].class);

    verify(jdbcTemplate)
        .query(searchSqlCaptor.capture(), eq(adresseRowMapper), searchParametersCaptor.capture());

    String normalizedSql = normalize(searchSqlCaptor.getValue());

    assertThat(normalizedSql)
        .contains("select * from adresse")
        .contains("where 1=1")
        .contains("order by id")
        .contains("limit ? offset ?")
        .doesNotContain("code_postal like ?")
        .doesNotContain("nom_voie like ?")
        .doesNotContain("nom_commune like ?");

    assertThat(searchParametersCaptor.getValue()).containsExactly(DEFAULT_PAGE_SIZE, 0L);
  }

  @Test
  void shouldSearchWithAllCriteria() {
    Pageable pageable = PageRequest.of(2, SEARCH_PAGE_SIZE);

    Adresse adresse = mock(Adresse.class);

    when(jdbcTemplate.query(anyString(), eq(adresseRowMapper), any(Object[].class)))
        .thenReturn(List.of(adresse));

    when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(Object[].class)))
        .thenReturn(EXPECTED_TOTAL_ELEMENTS);

    Page<Adresse> result = repository.rechercher("94700", "Victor", "Maisons", pageable);

    assertAll(
        () -> assertThat(result).isNotNull(),
        () -> assertThat(result.getContent()).containsExactly(adresse),
        () -> assertThat(result.getTotalElements()).isEqualTo(EXPECTED_TOTAL_ELEMENTS),
        () -> assertThat(result.getNumber()).isEqualTo(2),
        () -> assertThat(result.getSize()).isEqualTo(SEARCH_PAGE_SIZE));

    ArgumentCaptor<String> searchSqlCaptor = ArgumentCaptor.forClass(String.class);

    ArgumentCaptor<Object[]> searchParametersCaptor = ArgumentCaptor.forClass(Object[].class);

    verify(jdbcTemplate)
        .query(searchSqlCaptor.capture(), eq(adresseRowMapper), searchParametersCaptor.capture());

    String searchSql = normalize(searchSqlCaptor.getValue());

    assertThat(searchSql)
        .contains("code_postal like ?")
        .contains("nom_voie like ?")
        .contains("nom_commune like ?")
        .contains("order by id")
        .contains("limit ? offset ?");

    assertThat(searchParametersCaptor.getValue())
        .containsExactly("94700", "Victor%", "Maisons%", SEARCH_PAGE_SIZE, EXPECTED_OFFSET);

    ArgumentCaptor<String> countSqlCaptor = ArgumentCaptor.forClass(String.class);

    ArgumentCaptor<Object[]> countParametersCaptor = ArgumentCaptor.forClass(Object[].class);

    verify(jdbcTemplate)
        .queryForObject(countSqlCaptor.capture(), eq(Long.class), countParametersCaptor.capture());

    String countSql = normalize(countSqlCaptor.getValue());

    assertThat(countSql)
        .contains("select count(*) from adresse")
        .contains("code_postal like ?")
        .contains("nom_voie like ?")
        .contains("nom_commune like ?")
        .doesNotContain("limit")
        .doesNotContain("offset");

    assertThat(countParametersCaptor.getValue()).containsExactly("94700", "Victor%", "Maisons%");
  }

  @Test
  void shouldOnlyUseNonBlankCriteria() {
    Pageable pageable = PageRequest.of(0, PARTIAL_SEARCH_PAGE_SIZE);

    when(jdbcTemplate.query(anyString(), eq(adresseRowMapper), any(Object[].class)))
        .thenReturn(List.of());

    when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(Object[].class)))
        .thenReturn(0L);

    Page<Adresse> result = repository.rechercher("   ", "Victor", "", pageable);

    assertThat(result).isNotNull();
    assertThat(result.getTotalElements()).isZero();

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);

    ArgumentCaptor<Object[]> parametersCaptor = ArgumentCaptor.forClass(Object[].class);

    verify(jdbcTemplate)
        .query(sqlCaptor.capture(), eq(adresseRowMapper), parametersCaptor.capture());

    String sql = normalize(sqlCaptor.getValue());

    assertThat(sql)
        .contains("nom_voie like ?")
        .doesNotContain("code_postal like ?")
        .doesNotContain("nom_commune like ?");

    assertThat(parametersCaptor.getValue())
        .containsExactly("Victor%", PARTIAL_SEARCH_PAGE_SIZE, 0L);
  }

  @Test
  void shouldUseExactPostalCodeWithoutPercentSuffix() {
    Pageable pageable = PageRequest.of(0, SEARCH_PAGE_SIZE);

    when(jdbcTemplate.query(anyString(), eq(adresseRowMapper), any(Object[].class)))
        .thenReturn(List.of());

    when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(Object[].class)))
        .thenReturn(0L);

    repository.rechercher("94700", null, null, pageable);

    ArgumentCaptor<Object[]> parametersCaptor = ArgumentCaptor.forClass(Object[].class);

    verify(jdbcTemplate).query(anyString(), eq(adresseRowMapper), parametersCaptor.capture());

    assertThat(parametersCaptor.getValue()).containsExactly("94700", SEARCH_PAGE_SIZE, 0L);
  }

  @Test
  void shouldReturnNullWhenCountQueryReturnsNull() {
    Pageable pageable = PageRequest.of(0, SEARCH_PAGE_SIZE);

    when(jdbcTemplate.query(anyString(), eq(adresseRowMapper), any(Object[].class)))
        .thenReturn(List.of());

    when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(Object[].class)))
        .thenReturn(null);

    Page<Adresse> result = repository.rechercher(null, null, null, pageable);

    assertNull(result);
  }

  @Test
  void shouldNormalizeAutocompleteQueryAndAddWildcard() throws Exception {

    Adresse adresse = mock(Adresse.class);

    when(jdbcTemplate.query(anyString(), any(AdresseRowMapper.class), any(Object[].class)))
        .thenReturn(List.of(adresse));

    List<Adresse> result = repository.autoComplete("Rue   Victor-Hugo 94");

    assertThat(result).containsExactly(adresse);

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);

    ArgumentCaptor<RowMapper<Adresse>> mapperCaptor = rowMapperCaptor();

    ArgumentCaptor<Object[]> parametersCaptor = ArgumentCaptor.forClass(Object[].class);

    verify(jdbcTemplate)
        .query(sqlCaptor.capture(), mapperCaptor.capture(), parametersCaptor.capture());

    String sql = normalize(sqlCaptor.getValue());

    assertThat(sql)
        .contains("select a.*")
        .contains("from adresse a")
        .contains("join adresse_fts f")
        .contains("on a.id = f.id")
        .contains("and a.x = f.x")
        .contains("and a.y = f.y")
        .contains("and a.type_position = f.type_position")
        .contains("where f.search_text match ?")
        .contains("limit 10");

    assertThat(parametersCaptor.getValue()).containsExactly("rue* victor* hugo* 94*");

    assertThat(mapperCaptor.getValue()).isInstanceOf(AdresseRowMapper.class);
  }

  @Test
  void shouldRemoveSpecialCharactersFromAutocompleteQuery() throws Exception {

    when(jdbcTemplate.query(anyString(), any(AdresseRowMapper.class), any(Object[].class)))
        .thenReturn(List.of());

    repository.autoComplete("12, avenue de l'Église!");

    ArgumentCaptor<Object[]> parametersCaptor = ArgumentCaptor.forClass(Object[].class);

    verify(jdbcTemplate)
        .query(anyString(), any(AdresseRowMapper.class), parametersCaptor.capture());

    assertThat(parametersCaptor.getValue()).containsExactly("12* avenue* de* l* église*");
  }

  @Test
  void shouldReturnEmptyListForFindProches() {
    List<Adresse> result = repository.findProches(LATITUDE, LONGITUDE);

    assertThat(result).isEmpty();

    verify(jdbcTemplate, never()).query(anyString(), any(RowMapper.class), any(Object[].class));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private ArgumentCaptor<RowMapper<Adresse>> rowMapperCaptor() {
    return (ArgumentCaptor) ArgumentCaptor.forClass(RowMapper.class);
  }

  private String normalize(String sql) {
    return sql.replaceAll("\\s+", " ").trim().toLowerCase(Locale.ROOT);
  }
}
