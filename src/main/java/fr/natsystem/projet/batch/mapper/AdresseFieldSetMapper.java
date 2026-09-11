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
        fs.readString("id"),
        fs.readString("id_fantoir"),
        fs.readString("numero"),
        fs.readString("rep"),
        fs.readString("nom_voie"),
        fs.readString("code_postal"),
        fs.readString("code_insee"),
        fs.readString("nom_commune"),
        fs.readString("code_insee_ancienne_commune"),
        fs.readString("nom_ancienne_commune"),
        fs.readDouble("x"),
        fs.readDouble("y"),
        fs.readDouble("lon"),
        fs.readDouble("lat"),
        fs.readString("type_position"),
        fs.readString("alias"),
        fs.readString("nom_ld"),
        fs.readString("libelle_acheminement"),
        fs.readString("nom_afnor"),
        fs.readString("source_position"),
        fs.readString("source_nom_voie"),
        fs.readInt("certification_commune"),
        fs.readString("cad_parcelles"));
  }
}
