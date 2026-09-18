package fr.natsystem.projet.Mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.natsystem.projet.batch.mapper.AdresseRowMapper;
import fr.natsystem.projet.model.Adresse;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdresseRowMapperTest {

  private static final double X_COORDINATE = 651_234.56;
  private static final double Y_COORDINATE = 6_862_345.67;
  private static final double LONGITUDE = 2.3522;
  private static final double LATITUDE = 48.8566;

  @Mock private ResultSet resultSet;

  private AdresseRowMapper rowMapper;

  @BeforeEach
  void setUp() {
    rowMapper = new AdresseRowMapper();
  }

  @Test
  void shouldMapResultSetToAdresse() throws SQLException {
    when(resultSet.getString("id")).thenReturn("adresse-001");

    when(resultSet.getString("id_fantoir")).thenReturn("75101A001");

    when(resultSet.getString("numero")).thenReturn("10");

    when(resultSet.getString("rep")).thenReturn("B");

    when(resultSet.getString("nom_voie")).thenReturn("Rue de Rivoli");

    when(resultSet.getString("code_postal")).thenReturn("75001");

    when(resultSet.getString("code_insee")).thenReturn("75101");

    when(resultSet.getString("nom_commune")).thenReturn("Paris");

    when(resultSet.getString("code_insee_ancienne_commune")).thenReturn("75000");

    when(resultSet.getString("nom_ancienne_commune")).thenReturn("Ancien Paris");

    when(resultSet.getDouble("x")).thenReturn(X_COORDINATE);

    when(resultSet.getDouble("y")).thenReturn(Y_COORDINATE);

    when(resultSet.getDouble("lon")).thenReturn(LONGITUDE);

    when(resultSet.getDouble("lat")).thenReturn(LATITUDE);

    when(resultSet.getString("type_position")).thenReturn("entrée");

    when(resultSet.getString("alias")).thenReturn("Rivoli");

    when(resultSet.getString("nom_ld")).thenReturn("Centre-ville");

    when(resultSet.getString("libelle_acheminement")).thenReturn("PARIS");

    when(resultSet.getString("nom_afnor")).thenReturn("RUE DE RIVOLI");

    when(resultSet.getString("source_position")).thenReturn("BAN");

    when(resultSet.getString("source_nom_voie")).thenReturn("commune");

    when(resultSet.getInt("certification_commune")).thenReturn(1);

    when(resultSet.getString("cad_parcelles")).thenReturn("75101000AB0001");

    Adresse expectedAdresse =
        new Adresse(
            "adresse-001",
            "75101A001",
            "10",
            "B",
            "Rue de Rivoli",
            "75001",
            "75101",
            "Paris",
            "75000",
            "Ancien Paris",
            X_COORDINATE,
            Y_COORDINATE,
            LONGITUDE,
            LATITUDE,
            "entrée",
            "Rivoli",
            "Centre-ville",
            "PARIS",
            "RUE DE RIVOLI",
            "BAN",
            "commune",
            1,
            "75101000AB0001");

    Adresse actualAdresse = rowMapper.mapRow(resultSet, 0);

    assertThat(actualAdresse).usingRecursiveComparison().isEqualTo(expectedAdresse);

    verifyResultSetColumns();
  }

  private void verifyResultSetColumns() throws SQLException {
    verify(resultSet).getString("id");
    verify(resultSet).getString("id_fantoir");
    verify(resultSet).getString("numero");
    verify(resultSet).getString("rep");
    verify(resultSet).getString("nom_voie");
    verify(resultSet).getString("code_postal");
    verify(resultSet).getString("code_insee");
    verify(resultSet).getString("nom_commune");
    verify(resultSet).getString("code_insee_ancienne_commune");
    verify(resultSet).getString("nom_ancienne_commune");

    verify(resultSet).getDouble("x");
    verify(resultSet).getDouble("y");
    verify(resultSet).getDouble("lon");
    verify(resultSet).getDouble("lat");

    verify(resultSet).getString("type_position");
    verify(resultSet).getString("alias");
    verify(resultSet).getString("nom_ld");
    verify(resultSet).getString("libelle_acheminement");
    verify(resultSet).getString("nom_afnor");
    verify(resultSet).getString("source_position");
    verify(resultSet).getString("source_nom_voie");

    verify(resultSet).getInt("certification_commune");

    verify(resultSet).getString("cad_parcelles");
  }
}
