package fr.natsystem.projet.batch.mapper;

import fr.natsystem.projet.model.Dvf;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class DvfRowMapper implements RowMapper<Dvf> {

    @Override
    public Dvf mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Dvf(
                rs.getString("id_mutation"),
                rs.getObject("date_mutation", java.time.LocalDate.class),
                rs.getInt("numero_disposition"),
                rs.getString("nature_mutation"),
                rs.getDouble("valeur_fonciere"),
                rs.getString("adresse_numero"),
                rs.getString("adresse_suffixe"),
                rs.getString("adresse_code_voie"),
                rs.getString("adresse_nom_voie"),
                rs.getString("code_postal"),
                rs.getString("code_commune"),
                rs.getString("nom_commune"),
                rs.getString("ancien_code_commune"),
                rs.getString("ancien_nom_commune"),
                rs.getString("code_departement"),
                rs.getString("id_parcelle"),
                rs.getString("ancien_id_parcelle"),
                rs.getString("numero_volume"),
                rs.getString("lot_1_numero"),
                rs.getDouble("lot_1_surface_carrez"),
                rs.getString("lot_2_numero"),
                rs.getDouble("lot_2_surface_carrez"),
                rs.getString("lot_3_numero"),
                rs.getDouble("lot_3_surface_carrez"),
                rs.getString("lot_4_numero"),
                rs.getDouble("lot_4_surface_carrez"),
                rs.getString("lot_5_numero"),
                rs.getDouble("lot_5_surface_carrez"),
                rs.getInt("nombre_lots"),
                rs.getString("code_type_local"),
                rs.getString("type_local"),
                rs.getDouble("surface_reelle_bati"),
                rs.getInt("nombre_pieces_principales"),
                rs.getString("code_nature_culture"),
                rs.getString("nature_culture"),
                rs.getString("code_nature_culture_speciale"),
                rs.getString("nature_culture_speciale"),
                rs.getDouble("surface_terrain"),
                rs.getDouble("longitude"),
                rs.getDouble("latitude"),
                rs.getLong("id")
        );
    }
}