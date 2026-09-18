package fr.natsystem.projet.Mapper;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.natsystem.projet.batch.mapper.AdresseFieldSetMapper;
import fr.natsystem.projet.model.Adresse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.file.transform.FieldSet;

class AdresseFieldSetMapperTest {

  private static final double X_COORDINATE = 651234.50;
  private static final double Y_COORDINATE = 6861234.25;
  private static final double LONGITUDE = 2.3522;
  private static final double LATITUDE = 48.8566;

  private static final int CERTIFICATION_COMMUNE = 1;

  private AdresseFieldSetMapper mapper;
  private FieldSet fieldSet;

  @BeforeEach
  void setUp() {
    mapper = new AdresseFieldSetMapper();
    fieldSet = mock(FieldSet.class);
  }

  @Test
  void shouldMapAllFieldsToAdresse() throws Exception {
    when(fieldSet.readString("id")).thenReturn("ID-001");
    when(fieldSet.readString("id_fantoir")).thenReturn("FANTOIR-001");
    when(fieldSet.readString("numero")).thenReturn("12");
    when(fieldSet.readString("rep")).thenReturn("B");
    when(fieldSet.readString("nom_voie")).thenReturn("Rue de Paris");
    when(fieldSet.readString("code_postal")).thenReturn("75001");
    when(fieldSet.readString("code_insee")).thenReturn("75056");
    when(fieldSet.readString("nom_commune")).thenReturn("Paris");
    when(fieldSet.readString("code_insee_ancienne_commune")).thenReturn("75123");
    when(fieldSet.readString("nom_ancienne_commune")).thenReturn("Ancienne commune");

    when(fieldSet.readDouble("x")).thenReturn(X_COORDINATE);
    when(fieldSet.readDouble("y")).thenReturn(Y_COORDINATE);
    when(fieldSet.readDouble("lon")).thenReturn(LONGITUDE);
    when(fieldSet.readDouble("lat")).thenReturn(LATITUDE);

    when(fieldSet.readString("type_position")).thenReturn("entrée");
    when(fieldSet.readString("alias")).thenReturn("Alias voie");
    when(fieldSet.readString("nom_ld")).thenReturn("Lieu-dit");
    when(fieldSet.readString("libelle_acheminement")).thenReturn("PARIS");
    when(fieldSet.readString("nom_afnor")).thenReturn("RUE DE PARIS");
    when(fieldSet.readString("source_position")).thenReturn("commune");
    when(fieldSet.readString("source_nom_voie")).thenReturn("BAN");

    when(fieldSet.readInt("certification_commune")).thenReturn(CERTIFICATION_COMMUNE);

    when(fieldSet.readString("cad_parcelles")).thenReturn("75101000AB0012");

    Adresse result = mapper.mapFieldSet(fieldSet);

    assertAll(
        () -> assertEquals("ID-001", result.id()),
        () -> assertEquals("FANTOIR-001", result.id_fantoir()),
        () -> assertEquals("12", result.numero()),
        () -> assertEquals("B", result.rep()),
        () -> assertEquals("Rue de Paris", result.nom_voie()),
        () -> assertEquals("75001", result.code_postal()),
        () -> assertEquals("75056", result.code_insee()),
        () -> assertEquals("Paris", result.nom_commune()),
        () -> assertEquals("75123", result.code_insee_ancienne_commune()),
        () -> assertEquals("Ancienne commune", result.nom_ancienne_commune()),
        () -> assertEquals(X_COORDINATE, result.x()),
        () -> assertEquals(Y_COORDINATE, result.y()),
        () -> assertEquals(LONGITUDE, result.lon()),
        () -> assertEquals(LATITUDE, result.lat()),
        () -> assertEquals("entrée", result.type_position()),
        () -> assertEquals("Alias voie", result.alias()),
        () -> assertEquals("Lieu-dit", result.nom_ld()),
        () -> assertEquals("PARIS", result.libelle_acheminement()),
        () -> assertEquals("RUE DE PARIS", result.nom_afnor()),
        () -> assertEquals("commune", result.source_position()),
        () -> assertEquals("BAN", result.source_nom_voie()),
        () -> assertEquals(CERTIFICATION_COMMUNE, result.certification_commune()),
        () -> assertEquals("75101000AB0012", result.cad_parcelles()));
  }
}
