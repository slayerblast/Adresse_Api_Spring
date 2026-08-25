package fr.natsystem.projet.services;


import fr.natsystem.projet.batch.mapper.DvfRowMapper;
import fr.natsystem.projet.model.Dvf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Getter
@Setter
@StepScope
@RequiredArgsConstructor
public class DvfCacheService {
    private Map<Long, Dvf> cache = new HashMap<>(10000);
    @Value("#{stepExecutionContext['codeInsee']}")
    private String codeInsee;
    private final DvfRowMapper rowMapper;
    private String currentCodeInsee;
    private final JdbcTemplate jdbcTemplate;

    public void load(String codeInsee) {
        cache = new HashMap<>(10000);
        currentCodeInsee = codeInsee;

        // charger uniquement cette commune
        List<Dvf> dvfs =
                jdbcTemplate.query(
                        """
                        SELECT *
                        FROM dvf
                        WHERE code_commune = ?
                        """,
                        rowMapper,
                        codeInsee
                );

        for (Dvf dvf : dvfs) {
            cache.put(dvf.id(), dvf);
        }
    }
    public Dvf get(Long dvfId) {return cache.get(dvfId);}
    public void put(Long dvfId, Dvf dvf) {cache.put(dvfId, dvf);}

}
