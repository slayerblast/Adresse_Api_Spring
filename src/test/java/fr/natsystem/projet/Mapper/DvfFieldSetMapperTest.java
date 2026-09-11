package fr.natsystem.projet.Mapper;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import fr.natsystem.projet.batch.mapper.DvfFieldSetMapper;
import fr.natsystem.projet.model.Dvf;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.file.transform.DefaultFieldSet;
import org.springframework.batch.infrastructure.item.file.transform.FieldSet;

class DvfFieldSetMapperTest {

  private static final int TEST_YEAR = 2024;
  private static final int TEST_MONTH = 1;
  private static final int TEST_DAY = 15;

  private static final double TEST_PRICE = 250000.50;
  private static final double LOT_1_SURFACE = 45.75;
  private static final double LOT_2_SURFACE = 30.25;
  private static final double LOT_3_SURFACE = 20.50;
  private static final double LOT_4_SURFACE = 10.00;
  private static final double LOT_5_SURFACE = 5.50;
  private static final double MAIN_SURFACE = 120.75;
  private static final double GARDEN_SURFACE = 450.25;
  private static final double LONGITUDE = 2.3522;
  private static final double LATITUDE = 48.8566;

  private static final int FIELD_PRICE = 4;
  private static final int FIELD_LOT_1_SURFACE = 19;
  private static final int FIELD_LOT_2_SURFACE = 21;
  private static final int FIELD_LOT_3_SURFACE = 23;
  private static final int FIELD_LOT_4_SURFACE = 25;
  private static final int FIELD_LOT_5_SURFACE = 27;
  private static final int FIELD_LOT_COUNT = 28;
  private static final int FIELD_MAIN_SURFACE = 31;
  private static final int FIELD_MAIN_ROOM_COUNT = 32;
  private static final int FIELD_GARDEN_SURFACE = 37;
  private static final int FIELD_LONGITUDE = 38;
  private static final int FIELD_LATITUDE = 39;
  private static final int FIELD_ID = 40;

  private static final int NATURE_MUTATION_INDEX = 3;
  private static final int EXPECTED_LOT_COUNT = 5;
  private static final int EXPECTED_MAIN_ROOM_COUNT = 6;

  private static final int FIELD_COUNT = 41;
  private static final int DEFAULT_INTEGER_VALUE = 0;
  private static final long TEST_ID = 1001L;

  private static final String[] FIELD_NAMES = {
    "id_mutation",
    "date_mutation",
    "numero_disposition",
    "nature_mutation",
    "valeur_fonciere",
    "adresse_numero",
    "adresse_suffixe",
    "adresse_code_voie",
    "adresse_nom_voie",
    "code_postal",
    "code_commune",
    "nom_commune",
    "ancien_code_commune",
    "ancien_nom_commune",
    "code_departement",
    "id_parcelle",
    "ancien_id_parcelle",
    "numero_volume",
    "lot_1_numero",
    "lot_1_surface_carrez",
    "lot_2_numero",
    "lot_2_surface_carrez",
    "lot_3_numero",
    "lot_3_surface_carrez",
    "lot_4_numero",
    "lot_4_surface_carrez",
    "lot_5_numero",
    "lot_5_surface_carrez",
    "nombre_lots",
    "code_type_local",
    "type_local",
    "surface_reelle_bati",
    "nombre_pieces_principales",
    "code_nature_culture",
    "nature_culture",
    "code_nature_culture_speciale",
    "nature_culture_speciale",
    "surface_terrain",
    "longitude",
    "latitude",
    "id"
  };

  private DvfFieldSetMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new DvfFieldSetMapper();
  }

  @Test
  void shouldMapAllFieldsToDvf() throws Exception {
    FieldSet fieldSet =
        new DefaultFieldSet(
            new String[] {
              "2024-000001",
              "2024-01-15",
              "2",
              "Vente",
              "250000.50",
              "10",
              "B",
              "A001",
              "Rue de Paris",
              "75001",
              "75101",
              "Paris",
              "75000",
              "Ancien Paris",
              "75",
              "75101000AB0001",
              "75101000AA0001",
              "VOLUME-01",
              "LOT-001",
              "45.75",
              "LOT-002",
              "30.25",
              "LOT-003",
              "20.50",
              "LOT-004",
              "10.00",
              "LOT-005",
              "5.50",
              "5",
              "1",
              "Maison",
              "120.75",
              "6",
              "S",
              "Sols",
              "JARDIN",
              "Jardin d'agrément",
              "450.25",
              "2.3522",
              "48.8566",
              "1001"
            },
            FIELD_NAMES);

    Dvf expected =
        new Dvf(
            "2024-000001",
            LocalDate.of(TEST_YEAR, TEST_MONTH, TEST_DAY),
            2,
            "Vente",
            TEST_PRICE,
            "10",
            "B",
            "A001",
            "Rue de Paris",
            "75001",
            "75101",
            "Paris",
            "75000",
            "Ancien Paris",
            "75",
            "75101000AB0001",
            "75101000AA0001",
            "VOLUME-01",
            "LOT-001",
            LOT_1_SURFACE,
            "LOT-002",
            LOT_2_SURFACE,
            "LOT-003",
            LOT_3_SURFACE,
            "LOT-004",
            LOT_4_SURFACE,
            "LOT-005",
            LOT_5_SURFACE,
            EXPECTED_LOT_COUNT,
            "1",
            "Maison",
            MAIN_SURFACE,
            EXPECTED_MAIN_ROOM_COUNT,
            "S",
            "Sols",
            "JARDIN",
            "Jardin d'agrément",
            GARDEN_SURFACE,
            LONGITUDE,
            LATITUDE,
            TEST_ID);

    Dvf result = mapper.mapFieldSet(fieldSet);

    assertEquals(expected, result);
  }

  @Test
  void shouldReturnNullWhenDateIsEmpty() throws Exception {
    FieldSet fieldSet = createMinimalFieldSet("");

    Dvf result = mapper.mapFieldSet(fieldSet);

    assertNull(result.date_mutation());
  }

  @Test
  void shouldReturnNullWhenDateContainsOnlySpaces() throws Exception {
    FieldSet fieldSet = createMinimalFieldSet("   ");

    Dvf result = mapper.mapFieldSet(fieldSet);

    assertNull(result.date_mutation());
  }

  @Test
  void shouldUseZeroAsDefaultForEmptyIntegerFields() throws Exception {
    String[] values = createDefaultValues();

    values[1] = "2024-01-15";
    values[2] = "";
    values[FIELD_LOT_COUNT] = "";
    values[FIELD_MAIN_ROOM_COUNT] = "";

    Dvf result = mapper.mapFieldSet(new DefaultFieldSet(values, FIELD_NAMES));

    assertAll(
        () -> assertEquals(DEFAULT_INTEGER_VALUE, result.numero_disposition()),
        () -> assertEquals(DEFAULT_INTEGER_VALUE, result.nombre_lots()),
        () -> assertEquals(DEFAULT_INTEGER_VALUE, result.nombre_pieces_principales()));
  }

  @Test
  void shouldThrowExceptionWhenDateFormatIsInvalid() {
    FieldSet fieldSet = createMinimalFieldSet("15/01/2024");

    assertThrows(DateTimeParseException.class, () -> mapper.mapFieldSet(fieldSet));
  }

  private FieldSet createMinimalFieldSet(String dateMutation) {
    String[] values = createDefaultValues();

    values[0] = "2024-000001";
    values[1] = dateMutation;
    values[NATURE_MUTATION_INDEX] = "Vente";
    values[FIELD_ID] = String.valueOf(TEST_ID);

    return new DefaultFieldSet(values, FIELD_NAMES);
  }

  private String[] createDefaultValues() {
    String[] values = new String[FIELD_COUNT];

    for (int index = 0; index < values.length; index++) {
      values[index] = "";
    }

    values[FIELD_PRICE] = String.valueOf(DEFAULT_INTEGER_VALUE);
    values[FIELD_LOT_1_SURFACE] = String.valueOf(DEFAULT_INTEGER_VALUE);
    values[FIELD_LOT_2_SURFACE] = String.valueOf(DEFAULT_INTEGER_VALUE);
    values[FIELD_LOT_3_SURFACE] = String.valueOf(DEFAULT_INTEGER_VALUE);
    values[FIELD_LOT_4_SURFACE] = String.valueOf(DEFAULT_INTEGER_VALUE);
    values[FIELD_LOT_5_SURFACE] = String.valueOf(DEFAULT_INTEGER_VALUE);
    values[FIELD_MAIN_SURFACE] = String.valueOf(DEFAULT_INTEGER_VALUE);
    values[FIELD_GARDEN_SURFACE] = String.valueOf(DEFAULT_INTEGER_VALUE);
    values[FIELD_LONGITUDE] = String.valueOf(DEFAULT_INTEGER_VALUE);
    values[FIELD_LATITUDE] = String.valueOf(DEFAULT_INTEGER_VALUE);
    values[FIELD_ID] = String.valueOf(DEFAULT_INTEGER_VALUE);

    return values;
  }
}
