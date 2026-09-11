package fr.natsystem.projet.batch.step;

import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DvfSqlRequestTasklet implements Tasklet {
  private final JdbcTemplate jdbcTemplate;
  private static final String SQL_FILE = "sql/dvf_request.sql";

  @Override
  public @Nullable RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
      throws Exception {
    String sql =
        new String(
            new ClassPathResource(SQL_FILE).getInputStream().readAllBytes(),
            StandardCharsets.UTF_8);
    jdbcTemplate.update(sql);
    jdbcTemplate.update(
        """
                        TRUNCATE TABLE dvf_Staging;
                        """);

    jdbcTemplate.execute(
        """
            DROP INDEX IF EXISTS idx_staging_dvf_paging;
            """);
    return RepeatStatus.FINISHED;
  }
}
