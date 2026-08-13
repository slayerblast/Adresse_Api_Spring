package fr.natsystem.projet.batch.step;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Profile("postgres")
@Component
@RequiredArgsConstructor
public class CreateAdresseIndexPostgresTasklet implements CreateIndexInterface{
    private final JdbcTemplate jdbcTemplate;


    @Override
    public RepeatStatus execute(
            StepContribution contribution,
            ChunkContext chunkContext) {
        log.info("Create Adresse Index Postgres tasklet");
        jdbcTemplate.batchUpdate(
                """
                CREATE INDEX IF NOT EXISTS idx_adresse_rue
                ON adresse(LOWER(nom_voie) text_pattern_ops);
                """,
                """
                CREATE INDEX IF NOT EXISTS idx_adresse_codePostal
                ON adresse(code_postal text_pattern_ops);
                """,
                """
                CREATE INDEX IF NOT EXISTS idx_adresse_commune
                ON adresse(LOWER(nom_commune) text_pattern_ops);
                """,
                """
                CREATE INDEX IF NOT EXISTS idx_adresse_search
                ON adresse
                USING gin (search_text gin_trgm_ops);
                """
        );

        return RepeatStatus.FINISHED;
    }
}
