package fr.natsystem.projet.Service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.natsystem.projet.batch.mapper.AdresseRowMapper;
import fr.natsystem.projet.model.Adresse;
import fr.natsystem.projet.model.AdresseKey;
import fr.natsystem.projet.services.AdresseCacheService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class AdresseCacheServiceTest {

  private static final String CODE_INSEE = "94046";

  @Mock private AdresseRowMapper rowMapper;

  @Mock private JdbcTemplate jdbcTemplate;

  private AdresseCacheService service;

  @BeforeEach
  void setUp() {
    service = new AdresseCacheService(rowMapper, jdbcTemplate);
  }

  @Test
  void shouldLoadAddressesIntoCache() {
    AdresseKey firstKey = mockAdresseKey();
    AdresseKey secondKey = mockAdresseKey();

    Adresse firstAdresse = mockAdresseWithKey(firstKey);
    Adresse secondAdresse = mockAdresseWithKey(secondKey);

    when(jdbcTemplate.query(anyString(), same(rowMapper), any(Object[].class)))
        .thenReturn(List.of(firstAdresse, secondAdresse));

    service.load(CODE_INSEE);

    assertAll(
        () -> assertEquals(CODE_INSEE, service.getCurrentCodeInsee()),
        () -> assertSame(firstAdresse, service.get(firstKey)),
        () -> assertSame(secondAdresse, service.get(secondKey)),
        () -> assertEquals(2, service.getCache().size()));
  }

  @Test
  void shouldExecuteQueryWithProvidedCodeInsee() {
    when(jdbcTemplate.query(anyString(), same(rowMapper), any(Object[].class)))
        .thenReturn(List.of());

    service.load(CODE_INSEE);

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);

    ArgumentCaptor<Object[]> parametersCaptor = ArgumentCaptor.forClass(Object[].class);

    verify(jdbcTemplate).query(sqlCaptor.capture(), same(rowMapper), parametersCaptor.capture());

    String sql = normalize(sqlCaptor.getValue());
    Object[] parameters = parametersCaptor.getValue();

    assertAll(
        () -> assertTrue(sql.contains("SELECT * FROM adresse")),
        () -> assertTrue(sql.contains("WHERE code_insee = ?")),
        () -> assertEquals(1, parameters.length),
        () -> assertEquals(CODE_INSEE, parameters[0]));
  }

  @Test
  void shouldStoreCurrentCodeInseeWhenLoading() {
    when(jdbcTemplate.query(anyString(), same(rowMapper), any(Object[].class)))
        .thenReturn(List.of());

    service.load("75056");

    assertEquals("75056", service.getCurrentCodeInsee());
  }

  @Test
  void shouldKeepCacheEmptyWhenQueryReturnsNoAddress() {
    when(jdbcTemplate.query(anyString(), same(rowMapper), any(Object[].class)))
        .thenReturn(List.of());

    service.load(CODE_INSEE);

    assertAll(
        () -> assertTrue(service.getCache().isEmpty()),
        () -> assertEquals(CODE_INSEE, service.getCurrentCodeInsee()));
  }

  @Test
  void shouldResetPreviousCacheWhenLoadingAnotherCodeInsee() {
    AdresseKey oldKey = mockAdresseKey();
    Adresse oldAdresse = mockAdresse();

    service.put(oldKey, oldAdresse);

    assertSame(oldAdresse, service.get(oldKey));

    when(jdbcTemplate.query(anyString(), same(rowMapper), any(Object[].class)))
        .thenReturn(List.of());

    service.load("75056");

    assertAll(
        () -> assertNull(service.get(oldKey), "L'ancienne adresse ne doit plus être présente"),
        () -> assertTrue(service.getCache().isEmpty()),
        () -> assertEquals("75056", service.getCurrentCodeInsee()));
  }

  @Test
  void shouldReplaceCacheWithNewAddressesWhenReloading() {
    AdresseKey oldKey = mockAdresseKey();
    Adresse oldAdresse = mockAdresse();

    AdresseKey newKey = mockAdresseKey();
    Adresse newAdresse = mockAdresseWithKey(newKey);

    service.put(oldKey, oldAdresse);

    when(jdbcTemplate.query(anyString(), same(rowMapper), any(Object[].class)))
        .thenReturn(List.of(newAdresse));

    service.load("75056");

    assertAll(
        () -> assertNull(service.get(oldKey)),
        () -> assertSame(newAdresse, service.get(newKey)),
        () -> assertEquals(1, service.getCache().size()),
        () -> assertEquals("75056", service.getCurrentCodeInsee()));
  }

  @Test
  void shouldReturnAddressWhenKeyExists() {
    AdresseKey key = mockAdresseKey();
    Adresse adresse = mockAdresse();

    service.put(key, adresse);

    Adresse result = service.get(key);

    assertSame(adresse, result);
  }

  @Test
  void shouldReturnNullWhenKeyDoesNotExist() {
    AdresseKey key = mockAdresseKey();

    Adresse result = service.get(key);

    assertNull(result);
  }

  @Test
  void shouldPutAddressIntoCache() {
    AdresseKey key = mockAdresseKey();
    Adresse adresse = mockAdresse();

    service.put(key, adresse);

    assertAll(
        () -> assertSame(adresse, service.get(key)),
        () -> assertEquals(1, service.getCache().size()));
  }

  @Test
  void shouldReplaceAddressWhenSameKeyIsUsed() {
    AdresseKey key = mockAdresseKey();

    Adresse firstAdresse = mockAdresse();
    Adresse secondAdresse = mockAdresse();

    service.put(key, firstAdresse);
    service.put(key, secondAdresse);

    assertAll(
        () -> assertSame(secondAdresse, service.get(key)),
        () -> assertEquals(1, service.getCache().size()));
  }

  private AdresseKey mockAdresseKey() {
    return mock(AdresseKey.class);
  }

  /**
   * Adresse utilisée uniquement avec put() et get(). Son accesseur key() n'a pas besoin d'être
   * simulé.
   */
  private Adresse mockAdresse() {
    return mock(Adresse.class);
  }

  /**
   * Adresse retournée par JdbcTemplate dans load(). Le service appelle adresse.key() pour alimenter
   * le cache.
   */
  private Adresse mockAdresseWithKey(AdresseKey key) {
    Adresse adresse = mock(Adresse.class);

    when(adresse.key()).thenReturn(key);

    return adresse;
  }

  private String normalize(String sql) {
    return sql.replaceAll("\\s+", " ").trim();
  }
}
