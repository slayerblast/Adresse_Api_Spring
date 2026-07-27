package fr.natsystem.projet.batch.mapper;

import fr.natsystem.projet.model.Adresse;
import org.springframework.batch.infrastructure.item.file.mapping.FieldSetMapper;
import org.springframework.batch.infrastructure.item.file.transform.FieldSet;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;

@Component
public class AdresseFieldSetMapper implements FieldSetMapper<Adresse> {

    @Override
    public Adresse mapFieldSet(FieldSet fs) throws BindException {
        return new Adresse(
                fs.readString(0),   // id
                fs.readString(1),   // id_fantoir
                fs.readString(2),   // numero
                fs.readString(3),   // rep
                fs.readString(4),   // nom_voie
                fs.readString(5),   // code_postal
                fs.readString(6),   // code_insee
                fs.readString(7),   // nom_commune
                fs.readString(8),   // code_insee_ancienne_commune
                fs.readString(9),   // nom_ancienne_commune
                fs.readDouble(10),  // x
                fs.readDouble(11),  // y
                fs.readDouble(12),  // lon
                fs.readDouble(13),  // lat
                fs.readString(14),  // type_position
                fs.readString(15),  // alias
                fs.readString(16),  // nom_ld
                fs.readString(17),  // libelle_acheminement
                fs.readString(18),  // nom_afnor
                fs.readString(19),  // source_position
                fs.readString(20),  // source_nom_voie
                fs.readInt(21),     // certification_commune
                fs.readString(22)   // cad_parcelles
        );
    }
}
