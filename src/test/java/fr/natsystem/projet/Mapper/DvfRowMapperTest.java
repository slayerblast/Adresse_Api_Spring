package fr.natsystem.projet.Mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.natsystem.projet.batch.mapper.DvfRowMapper;
import fr.natsystem.projet.model.Dvf;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DvfRowMapperTest {

  private static final int TEST_YEAR = 2026;
  private static final int TEST_MONTH = 9;
  private static final int TEST_DAY = 4;

  private static final double PROPERTY_VALUE = 250_000.50;

  private static final double LOT_1_SURFACE = 45.5;
  private static final double LOT_2_SURFACE = 12.25;
  private static final double LOT_3_SURFACE = 8.75;
  private static final double LOT_4_SURFACE = 5.5;
  private static final double LOT_5_SURFACE = 3.25;

  private static final int LOT_COUNT = 5;
  private static final double BUILT_SURFACE = 75.25;
  private static final int ROOM_COUNT = 4;
  private static final double LAND_SURFACE = 350.75;

  private static final double LONGITUDE = 2.4385;
  private static final double LATITUDE = 48.8052;

  private static final long DVF_ID = 15L;

  private DvfRowMapper rowMapper;
  private ResultSet resultSet;

  @BeforeEach
  void setUp() {
    rowMapper = new DvfRowMapper();
    resultSet = mock(ResultSet.class);
  }

  @Test
  void shouldMapResultSetToDvf() throws SQLException {
    LocalDate dateMutation = LocalDate.of(TEST_YEAR, TEST_MONTH, TEST_DAY);

    when(resultSet.getString("id_mutation")).thenReturn("2026-001");

    when(resultSet.getObject("date_mutation", LocalDate.class)).thenReturn(dateMutation);

    when(resultSet.getInt("numero_disposition")).thenReturn(1);

    when(resultSet.getString("nature_mutation")).thenReturn("Vente");

    when(resultSet.getDouble("valeur_fonciere")).thenReturn(PROPERTY_VALUE);

    when(resultSet.getString("adresse_numero")).thenReturn("12");

    when(resultSet.getString("adresse_suffixe")).thenReturn("B");

    when(resultSet.getString("adresse_code_voie")).thenReturn("0123");

    when(resultSet.getString("adresse_nom_voie")).thenReturn("Rue Victor Hugo");

    when(resultSet.getString("code_postal")).thenReturn("94700");

    when(resultSet.getString("code_commune")).thenReturn("94046");

    when(resultSet.getString("nom_commune")).thenReturn("Maisons-Alfort");

    when(resultSet.getString("ancien_code_commune")).thenReturn("94000");

    when(resultSet.getString("ancien_nom_commune")).thenReturn("Ancien nom");

    when(resultSet.getString("code_departement")).thenReturn("94");

    when(resultSet.getString("id_parcelle")).thenReturn("94046000AB0123");

    when(resultSet.getString("ancien_id_parcelle")).thenReturn("94046000AB0099");

    when(resultSet.getString("numero_volume")).thenReturn("V01");

    when(resultSet.getString("lot_1_numero")).thenReturn("101");

    when(resultSet.getDouble("lot_1_surface_carrez")).thenReturn(LOT_1_SURFACE);

    when(resultSet.getString("lot_2_numero")).thenReturn("102");

    when(resultSet.getDouble("lot_2_surface_carrez")).thenReturn(LOT_2_SURFACE);

    when(resultSet.getString("lot_3_numero")).thenReturn("103");

    when(resultSet.getDouble("lot_3_surface_carrez")).thenReturn(LOT_3_SURFACE);

    when(resultSet.getString("lot_4_numero")).thenReturn("104");

    when(resultSet.getDouble("lot_4_surface_carrez")).thenReturn(LOT_4_SURFACE);

    when(resultSet.getString("lot_5_numero")).thenReturn("105");

    when(resultSet.getDouble("lot_5_surface_carrez")).thenReturn(LOT_5_SURFACE);

    when(resultSet.getInt("nombre_lots")).thenReturn(LOT_COUNT);

    when(resultSet.getString("code_type_local")).thenReturn("2");

    when(resultSet.getString("type_local")).thenReturn("Appartement");

    when(resultSet.getDouble("surface_reelle_bati")).thenReturn(BUILT_SURFACE);

    when(resultSet.getInt("nombre_pieces_principales")).thenReturn(ROOM_COUNT);

    when(resultSet.getString("code_nature_culture")).thenReturn("S");

    when(resultSet.getString("nature_culture")).thenReturn("Sols");

    when(resultSet.getString("code_nature_culture_speciale")).thenReturn("JARD");

    when(resultSet.getString("nature_culture_speciale")).thenReturn("Jardin");

    when(resultSet.getDouble("surface_terrain")).thenReturn(LAND_SURFACE);

    when(resultSet.getDouble("longitude")).thenReturn(LONGITUDE);

    when(resultSet.getDouble("latitude")).thenReturn(LATITUDE);

    when(resultSet.getLong("id")).thenReturn(DVF_ID);

    Dvf expected =
        new Dvf(
            "2026-001",
            dateMutation,
            1,
            "Vente",
            PROPERTY_VALUE,
            "12",
            "B",
            "0123",
            "Rue Victor Hugo",
            "94700",
            "94046",
            "Maisons-Alfort",
            "94000",
            "Ancien nom",
            "94",
            "94046000AB0123",
            "94046000AB0099",
            "V01",
            "101",
            LOT_1_SURFACE,
            "102",
            LOT_2_SURFACE,
            "103",
            LOT_3_SURFACE,
            "104",
            LOT_4_SURFACE,
            "105",
            LOT_5_SURFACE,
            LOT_COUNT,
            "2",
            "Appartement",
            BUILT_SURFACE,
            ROOM_COUNT,
            "S",
            "Sols",
            "JARD",
            "Jardin",
            LAND_SURFACE,
            LONGITUDE,
            LATITUDE,
            DVF_ID);

    Dvf actual = rowMapper.mapRow(resultSet, 0);

    assertEquals(expected, actual);
  }

  @Test
  void shouldMapNullDateMutation() throws SQLException {
    when(resultSet.getObject("date_mutation", LocalDate.class)).thenReturn(null);

    Dvf result = rowMapper.mapRow(resultSet, 0);

    assertEquals(null, result.date_mutation());
  }

  @Test
  void shouldPropagateSQLException() throws SQLException {
    SQLException expectedException = new SQLException("Colonne id_mutation absente");

    when(resultSet.getString("id_mutation")).thenThrow(expectedException);

    SQLException actualException =
        assertThrows(SQLException.class, () -> rowMapper.mapRow(resultSet, 0));

    assertSame(expectedException, actualException);
  }
}
