package fr.natsystem.projet.batch.mapper;

import fr.natsystem.projet.model.Dvf;
import org.springframework.batch.infrastructure.item.file.mapping.FieldSetMapper;
import org.springframework.batch.infrastructure.item.file.transform.FieldSet;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;

import java.time.LocalDate;

@Component
public class DvfFieldSetMapper implements FieldSetMapper<Dvf> {

    @Override
    public Dvf mapFieldSet(FieldSet fieldSet) throws BindException {
        return new Dvf(
                fieldSet.readString(0),          // id_mutation
                readLocalDate(fieldSet, 1),      // date_mutation
                fieldSet.readInt(2, 0),          // numero_disposition
                fieldSet.readString(3),          // nature_mutation
                fieldSet.readDouble(4),     // valeur_fonciere

                fieldSet.readString(5),          // adresse_numero
                fieldSet.readString(6),          // adresse_suffixe
                fieldSet.readString(7),          // adresse_code_voie
                fieldSet.readString(8),          // adresse_nom_voie
                fieldSet.readString(9),          // code_postal
                fieldSet.readString(10),         // code_commune
                fieldSet.readString(11),         // nom_commune
                fieldSet.readString(12),         // ancien_code_commune
                fieldSet.readString(13),         // ancien_nom_commune
                fieldSet.readString(14),         // code_departement

                fieldSet.readString(15),         // id_parcelle
                fieldSet.readString(16),         // ancien_id_parcelle
                fieldSet.readString(17),         // numero_volume

                fieldSet.readString(18),         // lot_1_numero
                fieldSet.readDouble(19),    // lot_1_surface_carrez

                fieldSet.readString(20),         // lot_2_numero
                fieldSet.readDouble(21),    // lot_2_surface_carrez

                fieldSet.readString(22),         // lot_3_numero
                fieldSet.readDouble(23),    // lot_3_surface_carrez

                fieldSet.readString(24),         // lot_4_numero
                fieldSet.readDouble(25),    // lot_4_surface_carrez

                fieldSet.readString(26),         // lot_5_numero
                fieldSet.readDouble(27),    // lot_5_surface_carrez

                fieldSet.readInt(28, 0),         // nombre_lots

                fieldSet.readString(29),         // code_type_local
                fieldSet.readString(30),         // type_local

                fieldSet.readDouble(31),    // surface_reelle_bati
                fieldSet.readInt(32, 0),         // nombre_pieces_principales

                fieldSet.readString(33),         // code_nature_culture
                fieldSet.readString(34),         // nature_culture

                fieldSet.readString(35),         // code_nature_culture_speciale
                fieldSet.readString(36),         // nature_culture_speciale

                fieldSet.readDouble(37),    // surface_terrain

                fieldSet.readDouble(38),    // longitude
                fieldSet.readDouble(39) ,    // latitude
                fieldSet.readLong(40)
        );
    }

    private LocalDate readLocalDate(FieldSet fieldSet, int index) {
        String value = fieldSet.readString(index);

        if (value == null || value.isBlank()) {
            return null;
        }

        return LocalDate.parse(value);
    }
}