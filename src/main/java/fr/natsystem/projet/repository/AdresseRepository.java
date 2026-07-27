package fr.natsystem.projet.repository;

import fr.natsystem.projet.batch.mapper.AdresseRowMapper;
import fr.natsystem.projet.model.Adresse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AdresseRepository {

    private final AdresseRowMapper adresseRowMapper;
    private final JdbcTemplate jdbcTemplate;

    public Page<Adresse> rechercher(
            String codePostal,
            String rue,
            String commune,
            Pageable pageable
    ) {
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (codePostal != null && !codePostal.isBlank()) {
            where.append(" AND code_postal = ?");
            params.add(codePostal);
        }

        if (rue != null && !rue.isBlank()) {
            where.append(" AND nom_voie LIKE ?");
            params.add("%" + rue + "%");
        }

        if (commune != null && !commune.isBlank()) {
            where.append(" AND nom_commune LIKE ?");
            params.add("%" + commune + "%");
        }

        // ------------------------
        // Requête de recherche
        // ------------------------

        String sql = """
                SELECT *
                FROM adresse
                """
                + where +
                """
                LIMIT ? OFFSET ?
                """;

        List<Object> searchParams = new ArrayList<>(params);
        searchParams.add(pageable.getPageSize());
        searchParams.add(pageable.getOffset());

        List<Adresse> adresses = jdbcTemplate.query(
                sql,
                adresseRowMapper,
                searchParams.toArray()
        );
        // ------------------------
        // Requête de comptage
        // ------------------------

        String countSql = """
                SELECT COUNT(*)
                FROM adresse
                """
                + where;

        Long total = jdbcTemplate.queryForObject(
                countSql,
                Long.class,
                params.toArray()
        );

        return new PageImpl<>(
                adresses,
                pageable,
                total
        );

    }
}