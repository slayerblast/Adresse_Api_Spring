package fr.natsystem.projet.batch.mapper;

import fr.natsystem.projet.model.Dvf;
import java.time.LocalDate;
import org.springframework.batch.infrastructure.item.file.mapping.FieldSetMapper;
import org.springframework.batch.infrastructure.item.file.transform.FieldSet;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;

@Component
public class DvfFieldSetMapper implements FieldSetMapper<Dvf> {

  @Override
  public Dvf mapFieldSet(FieldSet fieldSet) throws BindException {
    return new Dvf(
        fieldSet.readString("id_mutation"),
        readLocalDate(fieldSet),
        fieldSet.readInt("numero_disposition", 0),
        fieldSet.readString("nature_mutation"),
        fieldSet.readDouble("valeur_fonciere"),
        fieldSet.readString("adresse_numero"),
        fieldSet.readString("adresse_suffixe"),
        fieldSet.readString("adresse_code_voie"),
        fieldSet.readString("adresse_nom_voie"),
        fieldSet.readString("code_postal"),
        fieldSet.readString("code_commune"),
        fieldSet.readString("nom_commune"),
        fieldSet.readString("ancien_code_commune"),
        fieldSet.readString("ancien_nom_commune"),
        fieldSet.readString("code_departement"),
        fieldSet.readString("id_parcelle"),
        fieldSet.readString("ancien_id_parcelle"),
        fieldSet.readString("numero_volume"),
        fieldSet.readString("lot_1_numero"),
        fieldSet.readDouble("lot_1_surface_carrez"),
        fieldSet.readString("lot_2_numero"),
        fieldSet.readDouble("lot_2_surface_carrez"),
        fieldSet.readString("lot_3_numero"),
        fieldSet.readDouble("lot_3_surface_carrez"),
        fieldSet.readString("lot_4_numero"),
        fieldSet.readDouble("lot_4_surface_carrez"),
        fieldSet.readString("lot_5_numero"),
        fieldSet.readDouble("lot_5_surface_carrez"),
        fieldSet.readInt("nombre_lots", 0),
        fieldSet.readString("code_type_local"),
        fieldSet.readString("type_local"),
        fieldSet.readDouble("surface_reelle_bati"),
        fieldSet.readInt("nombre_pieces_principales", 0),
        fieldSet.readString("code_nature_culture"),
        fieldSet.readString("nature_culture"),
        fieldSet.readString("code_nature_culture_speciale"),
        fieldSet.readString("nature_culture_speciale"),
        fieldSet.readDouble("surface_terrain"),
        fieldSet.readDouble("longitude"),
        fieldSet.readDouble("latitude"),
        fieldSet.readLong("id"));
  }

  private LocalDate readLocalDate(FieldSet fieldSet) {

    String value = fieldSet.readString("date_mutation");

    return value == null || value.isBlank() ? null : LocalDate.parse(value);
  }
}
