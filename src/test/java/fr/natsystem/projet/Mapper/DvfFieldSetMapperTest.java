package fr.natsystem.projet.Mapper;


import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import fr.natsystem.projet.batch.mapper.DvfFieldSetMapper;
import fr.natsystem.projet.model.Dvf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.file.transform.DefaultFieldSet;
import org.springframework.batch.infrastructure.item.file.transform.FieldSet;



class DvfFieldSetMapperTest {

    private DvfFieldSetMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DvfFieldSetMapper();
    }

    @Test
    void shouldMapAllFieldsToDvf() throws Exception {
        FieldSet fieldSet = new DefaultFieldSet(
                new String[]{
                        "2024-000001",             // 0  id_mutation
                        "2024-01-15",              // 1  date_mutation
                        "2",                       // 2  numero_disposition
                        "Vente",                   // 3  nature_mutation
                        "250000.50",               // 4  valeur_fonciere

                        "10",                      // 5  adresse_numero
                        "B",                       // 6  adresse_suffixe
                        "A001",                    // 7  adresse_code_voie
                        "Rue de Paris",            // 8  adresse_nom_voie
                        "75001",                   // 9  code_postal
                        "75101",                   // 10 code_commune
                        "Paris",                   // 11 nom_commune
                        "75000",                   // 12 ancien_code_commune
                        "Ancien Paris",            // 13 ancien_nom_commune
                        "75",                      // 14 code_departement

                        "75101000AB0001",          // 15 id_parcelle
                        "75101000AA0001",          // 16 ancien_id_parcelle
                        "VOLUME-01",               // 17 numero_volume

                        "LOT-001",                 // 18 lot_1_numero
                        "45.75",                   // 19 lot_1_surface_carrez

                        "LOT-002",                 // 20 lot_2_numero
                        "30.25",                   // 21 lot_2_surface_carrez

                        "LOT-003",                 // 22 lot_3_numero
                        "20.50",                   // 23 lot_3_surface_carrez

                        "LOT-004",                 // 24 lot_4_numero
                        "10.00",                   // 25 lot_4_surface_carrez

                        "LOT-005",                 // 26 lot_5_numero
                        "5.50",                    // 27 lot_5_surface_carrez

                        "5",                       // 28 nombre_lots

                        "1",                       // 29 code_type_local
                        "Maison",                  // 30 type_local

                        "120.75",                  // 31 surface_reelle_bati
                        "6",                       // 32 nombre_pieces_principales

                        "S",                       // 33 code_nature_culture
                        "Sols",                    // 34 nature_culture

                        "JARDIN",                  // 35 code_nature_culture_speciale
                        "Jardin d'agrément",       // 36 nature_culture_speciale

                        "450.25",                  // 37 surface_terrain

                        "2.3522",                  // 38 longitude
                        "48.8566",                 // 39 latitude
                        "1001"                     // 40 id
                }
        );

        Dvf result = mapper.mapFieldSet(fieldSet);

        assertAll(
                () -> assertEquals(
                        "2024-000001",
                        result.id_mutation()
                ),
                () -> assertEquals(
                        LocalDate.of(2024, 1, 15),
                        result.date_mutation()
                ),
                () -> assertEquals(
                        2,
                        result.numero_disposition()
                ),
                () -> assertEquals(
                        "Vente",
                        result.nature_mutation()
                ),
                () -> assertEquals(
                        250000.50,
                        result.valeur_fonciere()
                ),

                () -> assertEquals(
                        "10",
                        result.adresse_numero()
                ),
                () -> assertEquals(
                        "B",
                        result.adresse_suffixe()
                ),
                () -> assertEquals(
                        "A001",
                        result.adresse_code_voie()
                ),
                () -> assertEquals(
                        "Rue de Paris",
                        result.adresse_nom_voie()
                ),
                () -> assertEquals(
                        "75001",
                        result.code_postal()
                ),
                () -> assertEquals(
                        "75101",
                        result.code_commune()
                ),
                () -> assertEquals(
                        "Paris",
                        result.nom_commune()
                ),
                () -> assertEquals(
                        "75000",
                        result.ancien_code_commune()
                ),
                () -> assertEquals(
                        "Ancien Paris",
                        result.ancien_nom_commune()
                ),
                () -> assertEquals(
                        "75",
                        result.code_departement()
                ),

                () -> assertEquals(
                        "75101000AB0001",
                        result.id_parcelle()
                ),
                () -> assertEquals(
                        "75101000AA0001",
                        result.ancien_id_parcelle()
                ),
                () -> assertEquals(
                        "VOLUME-01",
                        result.numero_volume()
                ),

                () -> assertEquals(
                        "LOT-001",
                        result.lot_1_numero()
                ),
                () -> assertEquals(
                        45.75,
                        result.lot_1_surface_carrez()
                ),
                () -> assertEquals(
                        "LOT-002",
                        result.lot_2_numero()
                ),
                () -> assertEquals(
                        30.25,
                        result.lot_2_surface_carrez()
                ),
                () -> assertEquals(
                        "LOT-003",
                        result.lot_3_numero()
                ),
                () -> assertEquals(
                        20.50,
                        result.lot_3_surface_carrez()
                ),
                () -> assertEquals(
                        "LOT-004",
                        result.lot_4_numero()
                ),
                () -> assertEquals(
                        10.00,
                        result.lot_4_surface_carrez()
                ),
                () -> assertEquals(
                        "LOT-005",
                        result.lot_5_numero()
                ),
                () -> assertEquals(
                        5.50,
                        result.lot_5_surface_carrez()
                ),

                () -> assertEquals(
                        5,
                        result.nombre_lots()
                ),

                () -> assertEquals(
                        "1",
                        result.code_type_local()
                ),
                () -> assertEquals(
                        "Maison",
                        result.type_local()
                ),

                () -> assertEquals(
                        120.75,
                        result.surface_reelle_bati()
                ),
                () -> assertEquals(
                        6,
                        result.nombre_pieces_principales()
                ),

                () -> assertEquals(
                        "S",
                        result.code_nature_culture()
                ),
                () -> assertEquals(
                        "Sols",
                        result.nature_culture()
                ),
                () -> assertEquals(
                        "JARDIN",
                        result.code_nature_culture_speciale()
                ),
                () -> assertEquals(
                        "Jardin d'agrément",
                        result.nature_culture_speciale()
                ),

                () -> assertEquals(
                        450.25,
                        result.surface_terrain()
                ),
                () -> assertEquals(
                        2.3522,
                        result.longitude()
                ),
                () -> assertEquals(
                        48.8566,
                        result.latitude()
                ),
                () -> assertEquals(
                        1001L,
                        result.id()
                )
        );
    }

    @Test
    void shouldReturnNullWhenDateIsEmpty() throws Exception {
        FieldSet fieldSet = createMinimalFieldSet("");

        Dvf result = mapper.mapFieldSet(fieldSet);

        assertNull(result.date_mutation());
    }

    @Test
    void shouldReturnNullWhenDateContainsOnlySpaces()
            throws Exception {

        FieldSet fieldSet = createMinimalFieldSet("   ");

        Dvf result = mapper.mapFieldSet(fieldSet);

        assertNull(result.date_mutation());
    }

    @Test
    void shouldUseZeroAsDefaultForEmptyIntegerFields()
            throws Exception {

        String[] values = createDefaultValues();

        values[1] = "2024-01-15";
        values[2] = "";
        values[28] = "";
        values[32] = "";

        Dvf result = mapper.mapFieldSet(
                new DefaultFieldSet(values)
        );

        assertAll(
                () -> assertEquals(
                        0,
                        result.numero_disposition()
                ),
                () -> assertEquals(
                        0,
                        result.nombre_lots()
                ),
                () -> assertEquals(
                        0,
                        result.nombre_pieces_principales()
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenDateFormatIsInvalid() {
        FieldSet fieldSet = createMinimalFieldSet(
                "15/01/2024"
        );

        assertThrows(
                DateTimeParseException.class,
                () -> mapper.mapFieldSet(fieldSet)
        );
    }

    private FieldSet createMinimalFieldSet(String dateMutation) {
        String[] values = createDefaultValues();

        values[0] = "2024-000001";
        values[1] = dateMutation;
        values[3] = "Vente";
        values[40] = "1001";

        return new DefaultFieldSet(values);
    }

    private String[] createDefaultValues() {
        String[] values = new String[41];

        for (int index = 0; index < values.length; index++) {
            values[index] = "";
        }

        /*
         * Les champs lus avec readDouble(index) n'ont pas de valeur
         * par défaut dans ton mapper. Ils doivent donc contenir
         * une valeur numérique pour que le mapping réussisse.
         */
        values[4] = "0";
        values[19] = "0";
        values[21] = "0";
        values[23] = "0";
        values[25] = "0";
        values[27] = "0";
        values[31] = "0";
        values[37] = "0";
        values[38] = "0";
        values[39] = "0";

        /*
         * Ton mapper utilise readLong(40) sans valeur par défaut.
         */
        values[40] = "0";

        return values;
    }
}