package fr.natsystem.projet.Step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.natsystem.projet.batch.step.CsvToStagingTasklet;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CsvToStagingTaskletTest {

  @TempDir Path tempDirectory;

  @Mock private JdbcTemplate jdbcTemplate;

  @Mock private DataSource dataSource;

  @Mock private Connection connection;

  @Mock private PGConnection pgConnection;

  @Mock private CopyManager copyManager;

  @Mock private StepContribution contribution;

  @Mock private StepExecution stepExecution;

  @Mock private JobExecution jobExecution;

  @Mock private JobParameters jobParameters;

  private CsvToStagingTasklet tasklet;

  @BeforeEach
  void setUp() throws Exception {
    tasklet = new CsvToStagingTasklet(jdbcTemplate, dataSource);

    ReflectionTestUtils.setField(tasklet, "pathFile", tempDirectory.toString());

    when(contribution.getStepExecution()).thenReturn(stepExecution);

    when(stepExecution.getJobExecution()).thenReturn(jobExecution);

    when(jobExecution.getJobParameters()).thenReturn(jobParameters);

    when(dataSource.getConnection()).thenReturn(connection);

    when(connection.unwrap(PGConnection.class)).thenReturn(pgConnection);

    when(pgConnection.getCopyAPI()).thenReturn(copyManager);
  }

  @Test
  void shouldImportAdresseCsv() throws Exception {
    Files.writeString(
        tempDirectory.resolve("adresses.csv"),
        """
                id;id_fantoir;numero
                adresse-001;75001;10
                """);

    when(jobParameters.getString("innerJob")).thenReturn("importAdresseJob");

    when(copyManager.copyIn(any(String.class), any(Reader.class))).thenReturn(1L);

    RepeatStatus result = tasklet.execute(contribution, null);

    assertEquals(RepeatStatus.FINISHED, result);

    verify(copyManager)
        .copyIn(
            argThat(sql -> sql.contains("COPY adresse_staging") && sql.contains("DELIMITER ';'")),
            any(Reader.class));

    verify(connection).close();
  }

  @Test
  void shouldImportDvfCsv() throws Exception {
    Files.writeString(
        tempDirectory.resolve("dvf.csv"),
        """
                id_mutation,date_mutation,numero_disposition
                mutation-001,2026-01-10,1
                """);

    when(jobParameters.getString("innerJob")).thenReturn("importDvfJob");

    when(copyManager.copyIn(any(String.class), any(Reader.class))).thenReturn(1L);

    RepeatStatus result = tasklet.execute(contribution, null);

    assertEquals(RepeatStatus.FINISHED, result);

    verify(copyManager)
        .copyIn(
            argThat(sql -> sql.contains("COPY dvf_staging") && sql.contains("DELIMITER ','")),
            any(Reader.class));

    verify(connection).close();
  }

  @Test
  void shouldImportAdresseCsvByDefaultWhenInnerJobIsUnknown() throws Exception {

    Files.writeString(
        tempDirectory.resolve("adresses.csv"),
        """
                id;id_fantoir;numero
                adresse-001;75001;10
                """);

    when(jobParameters.getString("innerJob")).thenReturn("unknownJob");

    when(copyManager.copyIn(any(String.class), any(Reader.class))).thenReturn(1L);

    RepeatStatus result = tasklet.execute(contribution, null);

    assertEquals(RepeatStatus.FINISHED, result);

    verify(copyManager)
        .copyIn(
            argThat(sql -> sql.contains("COPY adresse_staging") && sql.contains("DELIMITER ';'")),
            any(Reader.class));

    verify(connection).close();
  }

  @Test
  void shouldNotUseJdbcTemplate() throws Exception {
    Files.writeString(tempDirectory.resolve("adresses.csv"), "id;nom\nadresse-001;Test");

    when(jobParameters.getString("innerJob")).thenReturn("importAdresseJob");

    when(copyManager.copyIn(any(String.class), any(Reader.class))).thenReturn(1L);

    RepeatStatus result = tasklet.execute(contribution, null);

    assertEquals(RepeatStatus.FINISHED, result);

    verify(jdbcTemplate, never()).execute(any(String.class));
  }
}
