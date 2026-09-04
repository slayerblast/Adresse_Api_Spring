package fr.natsystem.projet.Mapper;

import fr.natsystem.projet.batch.mapper.DvfRowMapper;
import fr.natsystem.projet.model.Dvf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DvfRowMapperTest {

    private DvfRowMapper rowMapper;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() {
        rowMapper = new DvfRowMapper();
        resultSet = mock(ResultSet.class);
    }

    @Test
    void shouldMapResultSetToDvf() throws SQLException {
        LocalDate dateMutation = LocalDate.of(2026, 9, 4);

        when(resultSet.getString("id_mutation"))
                .thenReturn("2026-001");

        when(resultSet.getObject(
                "date_mutation",
                LocalDate.class
        )).thenReturn(dateMutation);

        when(resultSet.getInt("numero_disposition"))
                .thenReturn(1);

        when(resultSet.getString("nature_mutation"))
                .thenReturn("Vente");

        when(resultSet.getDouble("valeur_fonciere"))
                .thenReturn(250_000.50);

        when(resultSet.getString("adresse_numero"))
                .thenReturn("12");

        when(resultSet.getString("adresse_suffixe"))
                .thenReturn("B");

        when(resultSet.getString("adresse_code_voie"))
                .thenReturn("0123");

        when(resultSet.getString("adresse_nom_voie"))
                .thenReturn("Rue Victor Hugo");

        when(resultSet.getString("code_postal"))
                .thenReturn("94700");

        when(resultSet.getString("code_commune"))
                .thenReturn("94046");

        when(resultSet.getString("nom_commune"))
                .thenReturn("Maisons-Alfort");

        when(resultSet.getString("ancien_code_commune"))
                .thenReturn("94000");

        when(resultSet.getString("ancien_nom_commune"))
                .thenReturn("Ancien nom");

        when(resultSet.getString("code_departement"))
                .thenReturn("94");

        when(resultSet.getString("id_parcelle"))
                .thenReturn("94046000AB0123");

        when(resultSet.getString("ancien_id_parcelle"))
                .thenReturn("94046000AB0099");

        when(resultSet.getString("numero_volume"))
                .thenReturn("V01");

        when(resultSet.getString("lot_1_numero"))
                .thenReturn("101");

        when(resultSet.getDouble("lot_1_surface_carrez"))
                .thenReturn(45.5);

        when(resultSet.getString("lot_2_numero"))
                .thenReturn("102");

        when(resultSet.getDouble("lot_2_surface_carrez"))
                .thenReturn(12.25);

        when(resultSet.getString("lot_3_numero"))
                .thenReturn("103");

        when(resultSet.getDouble("lot_3_surface_carrez"))
                .thenReturn(8.75);

        when(resultSet.getString("lot_4_numero"))
                .thenReturn("104");

        when(resultSet.getDouble("lot_4_surface_carrez"))
                .thenReturn(5.5);

        when(resultSet.getString("lot_5_numero"))
                .thenReturn("105");

        when(resultSet.getDouble("lot_5_surface_carrez"))
                .thenReturn(3.25);

        when(resultSet.getInt("nombre_lots"))
                .thenReturn(5);

        when(resultSet.getString("code_type_local"))
                .thenReturn("2");

        when(resultSet.getString("type_local"))
                .thenReturn("Appartement");

        when(resultSet.getDouble("surface_reelle_bati"))
                .thenReturn(75.25);

        when(resultSet.getInt("nombre_pieces_principales"))
                .thenReturn(4);

        when(resultSet.getString("code_nature_culture"))
                .thenReturn("S");

        when(resultSet.getString("nature_culture"))
                .thenReturn("Sols");

        when(resultSet.getString("code_nature_culture_speciale"))
                .thenReturn("JARD");

        when(resultSet.getString("nature_culture_speciale"))
                .thenReturn("Jardin");

        when(resultSet.getDouble("surface_terrain"))
                .thenReturn(350.75);

        when(resultSet.getDouble("longitude"))
                .thenReturn(2.4385);

        when(resultSet.getDouble("latitude"))
                .thenReturn(48.8052);

        when(resultSet.getLong("id"))
                .thenReturn(15L);

        Dvf expected = new Dvf(
                "2026-001",
                dateMutation,
                1,
                "Vente",
                250_000.50,
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
                45.5,
                "102",
                12.25,
                "103",
                8.75,
                "104",
                5.5,
                "105",
                3.25,
                5,
                "2",
                "Appartement",
                75.25,
                4,
                "S",
                "Sols",
                "JARD",
                "Jardin",
                350.75,
                2.4385,
                48.8052,
                15L
        );

        Dvf actual = rowMapper.mapRow(resultSet, 0);

        assertEquals(expected, actual);
    }

    @Test
    void shouldMapNullDateMutation() throws SQLException {
        when(resultSet.getObject(
                "date_mutation",
                LocalDate.class
        )).thenReturn(null);

        Dvf result = rowMapper.mapRow(resultSet, 0);

        assertEquals(null, result.date_mutation());
    }

    @Test
    void shouldPropagateSQLException() throws SQLException {
        SQLException expectedException =
                new SQLException("Colonne id_mutation absente");

        when(resultSet.getString("id_mutation"))
                .thenThrow(expectedException);

        SQLException actualException = assertThrows(
                SQLException.class,
                () -> rowMapper.mapRow(resultSet, 0)
        );

        assertSame(expectedException, actualException);
    }
}