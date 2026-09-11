package fr.natsystem.projet.repository;

import fr.natsystem.projet.model.ContourCommune;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ContourRepository {
  private final JdbcTemplate jdbcTemplate;

  public List<ContourCommune> findAll() {

    String sql =
        """
                SELECT
                        c.insee,
                        c.nom,
                        t.prix_M2,
                        ST_AsGeoJSON(c.geom_simple) AS contour
                    FROM commune_contour c
                    JOIN tarif_commune t
                    ON t.code_insee = c.insee
            """;

    return jdbcTemplate.query(
        sql,
        (rs, rowNum) ->
            new ContourCommune(
                rs.getString("insee"),
                rs.getString("nom"),
                rs.getBigDecimal("prix_M2"),
                rs.getString("contour")));
  }
}
