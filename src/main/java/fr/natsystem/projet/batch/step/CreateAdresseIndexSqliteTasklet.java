package fr.natsystem.projet.batch.step;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;

import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Profile("sqlite")
@Component
@RequiredArgsConstructor
public class CreateAdresseIndexSqliteTasklet implements CreateIndexInterface{

    private final JdbcTemplate jdbcTemplate;


    @Override
    public RepeatStatus execute(
            StepContribution contribution,
            ChunkContext chunkContext) {

        jdbcTemplate.batchUpdate(
                """
                CREATE INDEX IF NOT EXISTS idx_adresse_rue
                ON adresse (nom_voie COLLATE NOCASE);
                """,
                """
                CREATE INDEX IF NOT EXISTS idx_adresse_codePostal
                ON adresse(code_postal COLLATE NOCASE);
                """,
                """
                CREATE INDEX IF NOT EXISTS idx_adresse_commune
                ON adresse (nom_commune COLLATE NOCASE);
                """
        );
        jdbcTemplate.update("""
                INSERT INTO adresse_fts(
                id,
                x,
                y,
                type_position,
                search_text
            )
            SELECT
                id,
                x,
                y,
                type_position,
                COALESCE(numero, '') || ' ' ||
                COALESCE(nom_voie, '') || ' ' ||
                COALESCE(code_postal, '') || ' ' ||
                COALESCE(nom_commune, '')
            FROM adresse;
            """);

        return RepeatStatus.FINISHED;
    }
}
