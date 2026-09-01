package fr.natsystem.projet.repository;

import fr.natsystem.projet.model.TarifCommune;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class TarifCommuneRepository {
    private final JdbcTemplate jdbcTemplate;

    public Optional<TarifCommune> findByCodeInsee(
            String codeInsee
    ) {
        String sql = """
            SELECT
                code_insee,

                prix_moyen,
                prix_median,
                prix_m2,
                nombre_transactions,

                prix_moyen_n_1,
                prix_median_n_1,
                prix_m2_n_1,
                nombre_transactions_n_1,

                variation_prix_moyen_pct,
                variation_prix_median_pct,
                variation_prix_m2_pct,
                variation_nombre_transactions_pct,

                date_debut_periode_n,
                date_fin_periode_n,
                date_debut_periode_n_1,
                date_fin_periode_n_1,

                date_calcul

            FROM tarif_commune
            WHERE code_insee = ?
            """;

        return jdbcTemplate.query(
                sql,
                (resultSet, rowNum) -> new TarifCommune(
                        resultSet.getString("code_insee"),

                        resultSet.getBigDecimal("prix_moyen"),
                        resultSet.getBigDecimal("prix_median"),
                        resultSet.getBigDecimal("prix_m2"),
                        resultSet.getObject("nombre_transactions", Long.class),

                        resultSet.getBigDecimal("prix_moyen_n_1"),
                        resultSet.getBigDecimal("prix_median_n_1"),
                        resultSet.getBigDecimal("prix_m2_n_1"),
                        resultSet.getObject("nombre_transactions_n_1", Long.class),

                        resultSet.getBigDecimal("variation_prix_moyen_pct"),
                        resultSet.getBigDecimal("variation_prix_median_pct"),
                        resultSet.getBigDecimal("variation_prix_m2_pct"),
                        resultSet.getBigDecimal("variation_nombre_transactions_pct"),

                        resultSet.getObject("date_debut_periode_n",java.time.LocalDate.class),
                        resultSet.getObject("date_fin_periode_n",java.time.LocalDate.class),
                        resultSet.getObject("date_debut_periode_n_1",java.time.LocalDate.class),
                        resultSet.getObject("date_fin_periode_n_1",java.time.LocalDate.class),

                        resultSet.getObject("date_calcul",java.time.OffsetDateTime.class)
                ),
                codeInsee
        ).stream().findFirst();
    }
}
