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
                }
        );

        Dvf expected = new Dvf(
                "2024-000001",
                LocalDate.of(2024, 1, 15),
                2,
                "Vente",
                250000.50,

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
                45.75,
                "LOT-002",
                30.25,
                "LOT-003",
                20.50,
                "LOT-004",
                10.00,
                "LOT-005",
                5.50,

                5,

                "1",
                "Maison",

                120.75,
                6,

                "S",
                "Sols",

                "JARDIN",
                "Jardin d'agrément",

                450.25,

                2.3522,
                48.8566,
                1001L
        );

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