package fr.natsystem.projet.batch.step;

import fr.natsystem.projet.batch.listener.BilanJobListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class SuppressionObsoleteTasklet implements Tasklet {
    private final BilanJobListener bilanJobListener;
    private final JdbcTemplate jdbcTemplate;


    @Override
    public RepeatStatus execute(
            StepContribution contribution,
            ChunkContext chunkContext) {

        int deleted =
                jdbcTemplate.update(
                        """
                                DELETE FROM adresse
                        WHERE NOT EXISTS (
                            SELECT 1
                            FROM adresse_staging
                            WHERE adresse_staging.id = adresse.id
                              AND adresse_staging.type_position = adresse.type_position
                              AND adresse_staging.x = adresse.x
                              AND adresse_staging.y = adresse.y
                        );
                        """
                );
        bilanJobListener.setObsolete(deleted);

        log.info("{} adresses obsolètes supprimées", bilanJobListener.getObsolete());
        jdbcTemplate.execute(
                """
                DROP TABLE adresse_staging;
                """
        );

        return RepeatStatus.FINISHED;
    }

}