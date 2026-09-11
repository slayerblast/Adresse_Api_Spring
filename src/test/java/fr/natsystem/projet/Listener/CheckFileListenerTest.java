package fr.natsystem.projet.Listener;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.natsystem.projet.batch.listener.CheckFileListener;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.test.util.ReflectionTestUtils;

class CheckFileListenerTest {

  private static final String TIMESTAMP = "20260904_143300";
  private static final String JOB_NAME = "checkFileJob";

  private static final int TEST_YEAR = 2026;
  private static final int TEST_MONTH = 9;
  private static final int TEST_DAY = 4;

  private static final int START_HOUR = 14;
  private static final int START_MINUTE = 30;
  private static final int END_MINUTE = 35;
  private static final int OTHER_END_MINUTE = 31;

  @TempDir Path temporaryDirectory;

  private CheckFileListener listener;

  @BeforeEach
  void setUp() {
    listener = new CheckFileListener();

    /*
     * Le code du listener concatène directement bilanDir
     * avec le nom du fichier. Il faut donc ajouter le séparateur.
     */
    ReflectionTestUtils.setField(
        listener,
        "bilanDir",
        temporaryDirectory.toString() + FileSystems.getDefault().getSeparator());

    /*
     * On fixe le timestamp afin de connaître exactement
     * le nom du fichier généré.
     */
    ReflectionTestUtils.setField(listener, "timestamp", TIMESTAMP);
  }

  @Test
  void shouldCreateReportWhenJobHasFailed() throws Exception {
    LocalDateTime startTime =
        LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, START_HOUR, START_MINUTE);

    LocalDateTime endTime =
        LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, START_HOUR, END_MINUTE);

    JobExecution jobExecution =
        createJobExecution(BatchStatus.FAILED, startTime, endTime, "NO_INPUT_FILE");

    listener.afterJob(jobExecution);

    Path reportPath = expectedReportPath();

    assertTrue(Files.exists(reportPath), "Le rapport doit être créé pour un job en échec");

    String reportContent = Files.readString(reportPath, StandardCharsets.UTF_8);

    assertAll(
        () -> assertTrue(reportContent.contains("=== STATUS DU JOB ===")),
        () -> assertTrue(reportContent.contains("Status : FAILED")),
        () -> assertTrue(reportContent.contains("ExitStatus : NO_INPUT_FILE")),
        () -> assertTrue(reportContent.contains("Début : " + startTime)),
        () -> assertTrue(reportContent.contains("Fin    : " + endTime)),
        () -> assertTrue(reportContent.contains("Durée totale : 300 secondes")),
        () -> assertTrue(reportContent.contains("Aucun fichier à traiter")));
  }

  @Test
  void shouldUseZeroDurationWhenStartTimeIsNull() throws Exception {

    LocalDateTime endTime =
        LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, START_HOUR, END_MINUTE);

    JobExecution jobExecution =
        createJobExecution(BatchStatus.FAILED, null, endTime, "NO_INPUT_FILE");

    listener.afterJob(jobExecution);

    String reportContent = Files.readString(expectedReportPath(), StandardCharsets.UTF_8);

    assertAll(
        () -> assertTrue(reportContent.contains("Durée totale : 0 secondes")),
        () -> assertTrue(reportContent.contains("Début : null")),
        () -> assertTrue(reportContent.contains("Fin    : " + endTime)));
  }

  @Test
  void shouldUseZeroDurationWhenEndTimeIsNull() throws Exception {

    LocalDateTime startTime =
        LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, START_HOUR, START_MINUTE);

    JobExecution jobExecution =
        createJobExecution(BatchStatus.FAILED, startTime, null, "NO_INPUT_FILE");

    listener.afterJob(jobExecution);

    String reportContent = Files.readString(expectedReportPath(), StandardCharsets.UTF_8);

    assertAll(
        () -> assertTrue(reportContent.contains("Durée totale : 0 secondes")),
        () -> assertTrue(reportContent.contains("Début : " + startTime)),
        () -> assertTrue(reportContent.contains("Fin    : null")));
  }

  @Test
  void shouldUseEmptyExitStatusWhenStatusIsMissing() throws Exception {

    JobExecution jobExecution =
        createJobExecution(
            BatchStatus.FAILED,
            LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, START_HOUR, START_MINUTE),
            LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, START_HOUR, OTHER_END_MINUTE),
            "");

    listener.afterJob(jobExecution);

    String reportContent = Files.readString(expectedReportPath(), StandardCharsets.UTF_8);

    assertTrue(reportContent.contains("ExitStatus : \n"));
  }

  @Test
  void shouldNotCreateReportWhenJobIsCompleted() throws Exception {

    JobExecution jobExecution =
        createJobExecution(
            BatchStatus.COMPLETED,
            LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, START_HOUR, START_MINUTE),
            LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, START_HOUR, END_MINUTE),
            "COMPLETED");

    listener.afterJob(jobExecution);

    assertFalse(
        Files.exists(expectedReportPath()), "Aucun rapport ne doit être créé pour un job terminé");

    assertDirectoryIsEmpty();
  }

  @Test
  void shouldNotCreateReportWhenJobIsStarted() throws Exception {

    JobExecution jobExecution =
        createJobExecution(
            BatchStatus.STARTED,
            LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, START_HOUR, START_MINUTE),
            null,
            "STARTED");

    listener.afterJob(jobExecution);

    assertFalse(Files.exists(expectedReportPath()));
    assertDirectoryIsEmpty();
  }

  private JobExecution createJobExecution(
      BatchStatus batchStatus,
      LocalDateTime startTime,
      LocalDateTime endTime,
      String lastDeciderStatus) {

    JobExecution jobExecution = mock(JobExecution.class);

    JobInstance jobInstance = mock(JobInstance.class);

    ExecutionContext executionContext = new ExecutionContext();

    executionContext.putString("lastDeciderStatus", lastDeciderStatus);

    when(jobExecution.getJobInstance()).thenReturn(jobInstance);

    when(jobInstance.getJobName()).thenReturn(JOB_NAME);

    when(jobExecution.getStatus()).thenReturn(batchStatus);

    when(jobExecution.getStartTime()).thenReturn(startTime);

    when(jobExecution.getEndTime()).thenReturn(endTime);

    when(jobExecution.getExecutionContext()).thenReturn(executionContext);

    return jobExecution;
  }

  private Path expectedReportPath() {
    return temporaryDirectory.resolve("rapport_" + JOB_NAME + "_" + TIMESTAMP + ".txt");
  }

  private void assertDirectoryIsEmpty() throws Exception {
    try (Stream<Path> files = Files.list(temporaryDirectory)) {
      assertEquals(0, files.count(), "Le répertoire temporaire doit rester vide");
    }
  }
}
