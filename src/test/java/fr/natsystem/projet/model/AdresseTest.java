package fr.natsystem.projet.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AdresseTest {

  private static final double KEY_LONGITUDE = 2.3522;
  private static final double KEY_LATITUDE = 48.8566;

  private static final double X_COORDINATE = 658_000.25;
  private static final double Y_COORDINATE = 6_859_000.75;
  private static final double LONGITUDE = 2.4385;
  private static final double LATITUDE = 48.8052;

  private static final int CERTIFIED = 1;
  private static final int NOT_CERTIFIED = 0;

  private static ValidatorFactory validatorFactory;
  private static Validator validator;

  @BeforeAll
  static void setUpValidator() {
    validatorFactory = Validation.buildDefaultValidatorFactory();
    validator = validatorFactory.getValidator();
  }

  @AfterAll
  static void closeValidatorFactory() {
    validatorFactory.close();
  }

  @Test
  void shouldBeBetterWhenCertifiedAndOtherIsNotCertified() {
    Adresse certifiedAdresse = createAdresse(CERTIFIED, "BAN", "GPS");

    Adresse nonCertifiedAdresse = createAdresse(NOT_CERTIFIED, "BAN", "GPS");

    boolean result = certifiedAdresse.isBetterThan(nonCertifiedAdresse);

    assertTrue(result);
  }

  @Test
  void shouldNotBeBetterWhenNotCertifiedAndOtherIsCertified() {
    Adresse nonCertifiedAdresse = createAdresse(NOT_CERTIFIED, "BAN", "GPS");

    Adresse certifiedAdresse = createAdresse(CERTIFIED, "BAN", "GPS");

    boolean result = nonCertifiedAdresse.isBetterThan(certifiedAdresse);

    assertFalse(result);
  }

  @Test
  void shouldBeBetterWhenOtherStreetNameSourceIsUnknown() {
    Adresse knownSourceAdresse = createAdresse(CERTIFIED, "BAN", "GPS");

    Adresse unknownSourceAdresse = createAdresse(CERTIFIED, "inconnue", "GPS");

    boolean result = knownSourceAdresse.isBetterThan(unknownSourceAdresse);

    assertTrue(result);
  }

  @Test
  void shouldDetectUnknownStreetNameSourceIgnoringCase() {
    Adresse knownSourceAdresse = createAdresse(CERTIFIED, "BAN", "GPS");

    Adresse unknownSourceAdresse = createAdresse(CERTIFIED, "INCONNUE", "GPS");

    boolean result = knownSourceAdresse.isBetterThan(unknownSourceAdresse);

    assertTrue(result);
  }

  @Test
  void shouldNotBeBetterWhenCurrentStreetNameSourceIsUnknown() {
    Adresse unknownSourceAdresse = createAdresse(CERTIFIED, "inconnue", "GPS");

    Adresse knownSourceAdresse = createAdresse(CERTIFIED, "BAN", "GPS");

    boolean result = unknownSourceAdresse.isBetterThan(knownSourceAdresse);

    assertFalse(result);
  }

  @Test
  void shouldBeBetterWhenOtherPositionSourceIsUnknown() {
    Adresse knownPositionAdresse = createAdresse(CERTIFIED, "BAN", "GPS");

    Adresse unknownPositionAdresse = createAdresse(CERTIFIED, "BAN", "inconnue");

    boolean result = knownPositionAdresse.isBetterThan(unknownPositionAdresse);

    assertTrue(result);
  }

  @Test
  void shouldDetectUnknownPositionSourceIgnoringCase() {
    Adresse knownPositionAdresse = createAdresse(CERTIFIED, "BAN", "GPS");

    Adresse unknownPositionAdresse = createAdresse(CERTIFIED, "BAN", "INCONNUE");

    boolean result = knownPositionAdresse.isBetterThan(unknownPositionAdresse);

    assertTrue(result);
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @CsvSource({
    "Même certification,                  BAN,      GPS",
    "Sources de nom de voie différentes,  CADASTRE, GPS",
    "Sources de position différentes,     BAN,      CADASTRE"
  })
  void shouldNotBeBetter(
      String scenario, String secondStreetNameSource, String secondPositionSource) {

    Adresse firstAdresse = createAdresse(CERTIFIED, "BAN", "GPS");

    Adresse secondAdresse = createAdresse(CERTIFIED, secondStreetNameSource, secondPositionSource);

    boolean result = firstAdresse.isBetterThan(secondAdresse);

    assertFalse(result);
  }

  @Test
  void certificationShouldHavePriorityOverSources() {
    Adresse certifiedAdresseWithUnknownSources = createAdresse(CERTIFIED, "inconnue", "inconnue");

    Adresse nonCertifiedAdresseWithKnownSources = createAdresse(NOT_CERTIFIED, "BAN", "GPS");

    boolean result =
        certifiedAdresseWithUnknownSources.isBetterThan(nonCertifiedAdresseWithKnownSources);

    assertTrue(result);
  }

  @Test
  void streetNameSourceShouldHavePriorityOverPositionSource() {
    Adresse currentAdresse = createAdresse(CERTIFIED, "BAN", "inconnue");

    Adresse otherAdresse = createAdresse(CERTIFIED, "inconnue", "GPS");

    boolean result = currentAdresse.isBetterThan(otherAdresse);

    assertTrue(result);
  }

  @Test
  void shouldCreateAdresseKeyFromExpectedProperties() {
    Adresse adresse = createAdresse(CERTIFIED, "BAN", "GPS");

    AdresseKey key = adresse.key();

    assertAll(
        () -> assertNotNull(key),
        () -> assertThat(key.id()).isEqualTo(adresse.id()),
        () -> assertThat(key.type_position()).isEqualTo(adresse.type_position()),
        () -> assertThat(key.x()).isEqualTo(adresse.x()),
        () -> assertThat(key.y()).isEqualTo(adresse.y()));
  }

  @Test
  void shouldCreateEqualKeysForSameIdentityProperties() {
    Adresse firstAdresse = createAdresse(CERTIFIED, "BAN", "GPS");

    Adresse secondAdresse =
        new Adresse(
            firstAdresse.id(),
            "autre-fantoir",
            "99",
            null,
            "Une autre voie",
            "75001",
            "75101",
            "Paris",
            null,
            null,
            firstAdresse.x(),
            firstAdresse.y(),
            KEY_LONGITUDE,
            KEY_LATITUDE,
            firstAdresse.type_position(),
            null,
            null,
            null,
            null,
            "GPS",
            "BAN",
            NOT_CERTIFIED,
            null);

    assertThat(firstAdresse.key()).isEqualTo(secondAdresse.key());
  }

  @Test
  void shouldBeValidWhenMandatoryPropertiesArePresent() {
    Adresse adresse = createAdresse(CERTIFIED, "BAN", "GPS");

    Set<ConstraintViolation<Adresse>> violations = validator.validate(adresse);

    assertThat(violations).isEmpty();
  }

  @Test
  void shouldRejectBlankId() {
    Adresse adresse =
        createAdresse(
            "",
            "1",
            "Rue Victor Hugo",
            "94046",
            "Maisons-Alfort",
            "entrée",
            new AdresseOptions("GPS", "BAN", CERTIFIED));

    assertViolation(adresse, "id");
  }

  @Test
  void shouldRejectBlankNumero() {
    Adresse adresse =
        createAdresse(
            "94046_001",
            "   ",
            "Rue Victor Hugo",
            "94046",
            "Maisons-Alfort",
            "entrée",
            new AdresseOptions("GPS", "BAN", CERTIFIED));

    assertViolation(adresse, "numero");
  }

  @Test
  void shouldRejectBlankStreetName() {
    Adresse adresse =
        createAdresse(
            "94046_001",
            "1",
            "",
            "94046",
            "Maisons-Alfort",
            "entrée",
            new AdresseOptions("GPS", "BAN", CERTIFIED));

    assertViolation(adresse, "nom_voie");
  }

  @Test
  void shouldRejectBlankCodeInsee() {
    Adresse adresse =
        createAdresse(
            "94046_001",
            "1",
            "Rue Victor Hugo",
            " ",
            "Maisons-Alfort",
            "entrée",
            new AdresseOptions("GPS", "BAN", CERTIFIED));

    assertViolation(adresse, "code_insee");
  }

  @Test
  void shouldRejectBlankCommuneName() {
    Adresse adresse =
        createAdresse(
            "94046_001",
            "1",
            "Rue Victor Hugo",
            "94046",
            "",
            "entrée",
            new AdresseOptions("GPS", "BAN", CERTIFIED));

    assertViolation(adresse, "nom_commune");
  }

  @Test
  void shouldRejectBlankPositionType() {
    Adresse adresse =
        createAdresse(
            "94046_001",
            "1",
            "Rue Victor Hugo",
            "94046",
            "Maisons-Alfort",
            "",
            new AdresseOptions("GPS", "BAN", CERTIFIED));

    assertViolation(adresse, "type_position");
  }

  @Test
  void shouldRejectBlankPositionSource() {
    Adresse adresse =
        createAdresse(
            "94046_001",
            "1",
            "Rue Victor Hugo",
            "94046",
            "Maisons-Alfort",
            "entrée",
            new AdresseOptions("", "BAN", CERTIFIED));

    assertViolation(adresse, "source_position");
  }

  @Test
  void shouldRejectBlankStreetNameSource() {
    Adresse adresse =
        createAdresse(
            "94046_001",
            "1",
            "Rue Victor Hugo",
            "94046",
            "Maisons-Alfort",
            "entrée",
            new AdresseOptions("GPS", "", CERTIFIED));

    assertViolation(adresse, "source_nom_voie");
  }

  private void assertViolation(Adresse adresse, String expectedProperty) {

    Set<ConstraintViolation<Adresse>> violations = validator.validate(adresse);

    assertThat(violations)
        .extracting(violation -> violation.getPropertyPath().toString())
        .contains(expectedProperty);
  }

  private Adresse createAdresse(
      int certificationCommune, String sourceNomVoie, String sourcePosition) {

    return createAdresse(
        "94046_001",
        "1",
        "Rue Victor Hugo",
        "94046",
        "Maisons-Alfort",
        "entrée",
        new AdresseOptions(sourcePosition, sourceNomVoie, certificationCommune));
  }

  private Adresse createAdresse(
      String id,
      String numero,
      String nomVoie,
      String codeInsee,
      String nomCommune,
      String typePosition,
      AdresseOptions options) {

    return new Adresse(
        id,
        "940460001",
        numero,
        null,
        nomVoie,
        "94700",
        codeInsee,
        nomCommune,
        null,
        null,
        X_COORDINATE,
        Y_COORDINATE,
        LONGITUDE,
        LATITUDE,
        typePosition,
        null,
        null,
        "MAISONS-ALFORT",
        "RUE VICTOR HUGO",
        options.sourcePosition(),
        options.sourceNomVoie(),
        options.certificationCommune(),
        null);
  }

  private record AdresseOptions(
      String sourcePosition, String sourceNomVoie, int certificationCommune) {}
}
