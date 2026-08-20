package fr.natsystem.projet.repository;

import fr.natsystem.projet.batch.mapper.AdresseRowMapper;
import fr.natsystem.projet.model.Adresse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

@Profile("postgres")
@Repository
@RequiredArgsConstructor
public class AdresseRepositoryPostgres implements AdresseRepository {

    private final AdresseRowMapper adresseRowMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Page<Adresse> rechercher(
            String codePostal,
            String rue,
            String commune,
            Pageable pageable
    ) {
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (codePostal != null && !codePostal.isBlank()) {
            where.append(" AND code_postal LIKE ? ");
            params.add(codePostal + "%");
        }

        if (rue != null && !rue.isBlank()) {
            where.append(" AND LOWER(nom_voie) LIKE ? ");
            params.add(rue + "%");
        }

        if (commune != null && !commune.isBlank()) {
            where.append(" AND LOWER(nom_commune) LIKE ? ");
            params.add(commune + "%");
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
                ORDER BY id
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

    @Override
    public List<Adresse> autoComplete(String q) {

        String search = Normalizer.normalize(
                        q,
                        Normalizer.Form.NFD
                )
                .replaceAll("\\p{M}", "")
                .replace("-", " ")
                .replace("'", " ")
                .toLowerCase()
                .trim();

        String sql = """
       SELECT *
       FROM adresse
       ORDER BY similarity(search_text, ?) DESC
       LIMIT 10;
        """;

        return jdbcTemplate.query(
                sql,
                new AdresseRowMapper(),
                search
        );
    }

    @Override
    public List<Adresse> findProches(double lat, double lon) {

        String sql = """
        SELECT *,
               ST_Distance(
                   position,
                   ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography
               ) AS distance
        FROM adresse
        WHERE ST_DWithin(
            position,
            ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography,
            50
        )
        ORDER BY distance
        LIMIT 1
        """;

        return jdbcTemplate.query(
                sql,
                adresseRowMapper,
                lon, lat,
                lon, lat
        );
    }
}