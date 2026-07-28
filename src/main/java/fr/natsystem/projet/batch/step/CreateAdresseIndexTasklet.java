package fr.natsystem.projet.batch.step;

import fr.natsystem.projet.batch.listener.BilanJobListener;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;

import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateAdresseIndexTasklet implements Tasklet{
    private final JdbcTemplate jdbcTemplate;

    @Override
    public RepeatStatus execute(
            StepContribution contribution,
            ChunkContext chunkContext) {

        jdbcTemplate.batchUpdate(
                """
                CREATE INDEX IF NOT EXISTS idx_adresse_rue
                ON adresse(nom_voie)
                """,
                """
                CREATE INDEX IF NOT EXISTS idx_adresse_codePostal
                ON adresse(code_postal)
                """,
                """
                CREATE INDEX IF NOT EXISTS idx_adresse_commune
                ON adresse(nom_commune)
                """
        );

        return RepeatStatus.FINISHED;
    }
}
