package fr.natsystem.projet.services;

import fr.natsystem.projet.batch.mapper.AdresseRowMapper;

import fr.natsystem.projet.model.Adresse;
import fr.natsystem.projet.model.AdresseKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Getter
@Setter
@RequiredArgsConstructor
public class AdresseCacheService {
    private Map<AdresseKey, Adresse> cache = new HashMap<>(10000);

    private final AdresseRowMapper rowMapper;
    private String currentCodeInsee;
    private final JdbcTemplate jdbcTemplate;

    public void load(String codeInsee) {

        cache = new HashMap<>(10000);
        currentCodeInsee = codeInsee;

        // charger uniquement cette commune
        List<Adresse> adresses =
                jdbcTemplate.query(
                        """
                        SELECT *
                        FROM adresse
                        WHERE code_insee = ?
                        """,
                        rowMapper,
                        codeInsee
                );

        for (Adresse adresse : adresses) {
            cache.put(adresse.key(), adresse);
        }

    }

    public Adresse get(AdresseKey key) {
        return cache.get(key);
    }

    public void put(AdresseKey key, Adresse adresse) {
        cache.put(key, adresse);
    }
}
