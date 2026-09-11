package fr.natsystem.projet.batch.step;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateStagingIndexTasklet implements Tasklet {

  private final JdbcTemplate jdbcTemplate;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    String innerJob =
        contribution.getStepExecution().getJobExecution().getJobParameters().getString("innerJob");
    if (innerJob != null) {
      if (innerJob.equals("importAdresseJob")) {
        jdbcTemplate.batchUpdate(
            """
                        CREATE INDEX IF NOT EXISTS idx_adresse_code_insee
                        ON adresse(code_insee)
                        """,
            """
                        CREATE INDEX IF NOT EXISTS idx_staging_paging
                        ON adresse_staging(code_insee, id)
                        """,
            """
                        CREATE INDEX IF NOT EXISTS idx_staging_key
                        ON adresse_staging(id, type_position, x, y)
                        """);
      } else if (innerJob.equals("importDvfJob")) {
        jdbcTemplate.batchUpdate(
            """
                        CREATE INDEX IF NOT EXISTS idx_dvf_code_commune
                        ON dvf(code_commune)
                        """,
            """
                        CREATE INDEX IF NOT EXISTS idx_staging_dvf_paging
                        ON dvf_staging(code_commune, id)
                        """);
      } else {
        jdbcTemplate.batchUpdate(
            """
                        CREATE INDEX IF NOT EXISTS idx_adresse_code_insee
                        ON adresse(code_insee)
                        """,
            """
                        CREATE INDEX IF NOT EXISTS idx_staging_paging
                        ON adresse_staging(code_insee, id)
                        """,
            """
                        CREATE INDEX IF NOT EXISTS idx_staging_key
                        ON adresse_staging(id, type_position, x, y)
                        """);
      }
    }

    return RepeatStatus.FINISHED;
  }
}
