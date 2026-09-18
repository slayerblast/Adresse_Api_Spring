package fr.natsystem.projet.Model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import fr.natsystem.projet.model.Adresse;
import fr.natsystem.projet.model.AdresseValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.batch.infrastructure.item.validator.ValidationException;

class AdresseValidatorTest {

  private static final double X_COORDINATE = 651_000.0;
  private static final double Y_COORDINATE = 6_862_000.0;
  private static final double LONGITUDE = 2.4385;
  private static final double LATITUDE = 48.8052;

  private AdresseValidator validator;

  @BeforeEach
  void setUp() {
    validator = new AdresseValidator();
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @CsvSource({
    "Code métropolitain,       9404600001,  94046",
    "Code corse en minuscules, 2a0040001,   2a004",
    "Code outre-mer,           971230001,   97123",
    "Code INSEE normalisé,     2a0040001,   2A004"
  })
  void shouldAcceptValidInseeCode(String scenario, String id, String codeInsee) {
    Adresse adresse = createAdresse(id, codeInsee);

    assertDoesNotThrow(() -> validator.validate(adresse));
  }

  @Test
  void shouldThrowValidationExceptionWhenInseeCodeIsInvalid() {
    Adresse adresse = createAdresse("990010001", "99001");

    ValidationException exception =
        assertThrows(ValidationException.class, () -> validator.validate(adresse));

    assertEquals("Code INSEE invalide", exception.getMessage());
  }

  @Test
  void shouldThrowValidationExceptionWhenInseeCodeIsTooShort() {
    Adresse adresse = createAdresse("94040001", "9404");

    ValidationException exception =
        assertThrows(ValidationException.class, () -> validator.validate(adresse));

    assertEquals("Code INSEE invalide", exception.getMessage());
  }

  @Test
  void shouldThrowValidationExceptionWhenIdDoesNotStartWithInseeCode() {
    Adresse adresse = createAdresse("7505600001", "94046");

    ValidationException exception =
        assertThrows(ValidationException.class, () -> validator.validate(adresse));

    assertEquals("L'id ne commence pas par le code INSEE : 94046", exception.getMessage());
  }

  private Adresse createAdresse(String id, String codeInsee) {
    return new Adresse(
        id,
        "FANTOIR-001",
        "10",
        "",
        "Rue de Paris",
        "94700",
        codeInsee,
        "Maisons-Alfort",
        "",
        "",
        X_COORDINATE,
        Y_COORDINATE,
        LONGITUDE,
        LATITUDE,
        "entrée",
        "",
        "",
        "MAISONS-ALFORT",
        "RUE DE PARIS",
        "BAN",
        "BAN",
        1,
        "");
  }
}
