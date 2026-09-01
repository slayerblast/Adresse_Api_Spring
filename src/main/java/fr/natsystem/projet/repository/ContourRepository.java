package fr.natsystem.projet.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.natsystem.projet.model.ContourCommune;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ContourRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    public List<ContourCommune> findAll() {

        String sql = """
                SELECT
                        c.insee,
                        c.nom,
                        t.prix_Moyen,
                        ST_AsGeoJSON(
                            ST_SimplifyPreserveTopology(c.geom_simple, 0.001)
                        ) AS contour
                    FROM commune_contour c
                    JOIN tarif_commune t
                    ON t.code_insee = c.insee
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                        return new ContourCommune(
                                rs.getString("insee"),
                                rs.getString("nom"),
                                rs.getBigDecimal("prix_Moyen"),
                                rs.getString("contour")
                        );
                }
        );
    }
}
