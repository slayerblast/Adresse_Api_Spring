package fr.natsystem.projet.Listener;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.natsystem.projet.batch.listener.AdresseSkipListener;
import fr.natsystem.projet.batch.listener.BilanJobListener;
import fr.natsystem.projet.batch.listener.DvfSkipListener;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;

class BilanJobListenerTest {

  private static final long PARENT_JOB_ID = 100L;

  private static final long DVF_REJECTED_ID_1 = 201L;
  private static final long DVF_REJECTED_ID_2 = 202L;
  private static final long DVF_REJECTED_ID_3 = 203L;

  private static final int TEST_READ_COUNT = 10;
  private static final int TEST_WRITE_COUNT = 10;
  private static final int TEST_FAILED_READ_COUNT = 10;
  private static final int TEST_FAILED_WRITE_COUNT = 5;

  private static final int CSV_READ_COUNT = 100;
  private static final int CSV_WRITE_COUNT = 98;
  private static final int IMPORT_READ_COUNT = 98;
  private static final int IMPORT_WRITE_COUNT = 95;

  private static final int DVF_READ_COUNT = 20;
  private static final int DVF_WRITE_COUNT = 20;
  private static final int DVF_IMPORT_WRITE_COUNT = 17;

  private static final int DUPLICATE_COUNT = 2;
  private static final int PURE_DUPLICATE_COUNT = 1;
  private static final int OBSOLETE_COUNT = 3;

  private static final int TEST_YEAR = 2026;
  private static final int TEST_MONTH = 9;
  private static final int TEST_DAY = 4;

  private static final int PARENT_START_HOUR = 14;
  private static final int PARENT_START_MINUTE = 30;

  private static final int JOB_END_HOUR = 14;
  private static final int JOB_END_MINUTE = 35;

  private static final int STEP_END_SECOND = 5;

  private static final LocalDateTime PARENT_START_TIME =
      LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, PARENT_START_HOUR, PARENT_START_MINUTE);

  private static final LocalDateTime JOB_END_TIME =
      LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, JOB_END_HOUR, JOB_END_MINUTE);

  private static final LocalDateTime STEP_START_TIME = PARENT_START_TIME;

  private static final LocalDateTime STEP_END_TIME =
      LocalDateTime.of(
          TEST_YEAR, TEST_MONTH, TEST_DAY, PARENT_START_HOUR, PARENT_START_MINUTE, STEP_END_SECOND);

  private static final String TIMESTAMP = "20260904_143400";

  @TempDir Path temporaryDirectory;

  private AdresseSkipListener skipListener;
  private DvfSkipListener dvfSkipListener;
  private JobRepository jobRepository;

  private BilanJobListener listener;

  @BeforeEach
  void setUp() {
    skipListener = mock(AdresseSkipListener.class);
    dvfSkipListener = mock(DvfSkipListener.class);
    jobRepository = mock(JobRepository.class);

    listener = new BilanJobListener(skipListener, dvfSkipListener, jobRepository);

    listener.setBilanDir(temporaryDirectory.toString());
    listener.setTimestamp(TIMESTAMP);

    when(skipListener.getIdsRejetes()).thenReturn(List.of());

    when(dvfSkipListener.getIdsRejetes()).thenReturn(List.of());
  }

  @Test
  void shouldWriteAdresseReportsAfterJob() throws Exception {
    when(skipListener.getIdsRejetes()).thenReturn(List.of("101", "102"));

    JobExecution parentExecution = createParentExecution();
    JobExecution jobExecution =
        createJobExecution(
            "importAdresseJob",
            BatchStatus.COMPLETED,
            Set.of(
                createStep(
                    "csvToStagingStep", BatchStatus.COMPLETED, CSV_READ_COUNT, CSV_WRITE_COUNT),
                createStep(
                    "masterStepAdresse",
                    BatchStatus.COMPLETED,
                    IMPORT_READ_COUNT,
                    IMPORT_WRITE_COUNT)));

    when(jobRepository.getJobExecution(PARENT_JOB_ID)).thenReturn(parentExecution);

    listener.getDoublon().set(DUPLICATE_COUNT);
    listener.getDoublonPur().set(PURE_DUPLICATE_COUNT);
    listener.setObsolete(OBSOLETE_COUNT);

    listener.afterJob(jobExecution);

    Path mainReport = temporaryDirectory.resolve("rapport_importAdresseJob_" + TIMESTAMP + ".txt");

    Path failureReport = temporaryDirectory.resolve("bilan_failed.txt");

    Path rejectedReport =
        temporaryDirectory.resolve("rapport_rejetes_importAdresseJob_" + TIMESTAMP + ".txt");

    assertAll(
        () -> assertTrue(Files.exists(mainReport)),
        () -> assertTrue(Files.exists(failureReport)),
        () -> assertTrue(Files.exists(rejectedReport)));

    String mainContent = Files.readString(mainReport, StandardCharsets.UTF_8);

    assertAll(
        () -> assertTrue(mainContent.contains("=== STATUS DU JOB ===")),
        () -> assertTrue(mainContent.contains("Job parent : parentJob")),
        () -> assertTrue(mainContent.contains("Status : COMPLETED")),
        () -> assertTrue(mainContent.contains("ExitStatus : COMPLETED")),
        () -> assertTrue(mainContent.contains("=== BILAN DES TEMPS PAR ETAPES ===")),
        () -> assertTrue(mainContent.contains("=== BILAN IMPORT CSV -> STAGING ===")),
        () -> assertTrue(mainContent.contains("Checksum du fichier : checksum-test")),
        () -> assertTrue(mainContent.contains("=== BILAN IMPORT ===")),
        () -> assertTrue(mainContent.contains("ReadCount  : " + CSV_WRITE_COUNT)),
        () -> assertTrue(mainContent.contains("WriteCount : " + IMPORT_WRITE_COUNT)),
        () -> assertTrue(mainContent.contains("Doublons purs : " + PURE_DUPLICATE_COUNT)),
        () -> assertTrue(mainContent.contains("Lignes en double : " + DUPLICATE_COUNT)),
        () -> assertTrue(mainContent.contains("Nombre d'ID rejetés : 2")),
        () -> assertTrue(mainContent.contains("Lignes obsolètes supprimées : " + OBSOLETE_COUNT)));

    String failureContent = Files.readString(failureReport, StandardCharsets.UTF_8);

    assertTrue(failureContent.contains("Aucune erreur détectée."));

    String rejectedContent = Files.readString(rejectedReport, StandardCharsets.UTF_8);

    assertAll(
        () -> assertTrue(rejectedContent.contains("Nombre de lignes rejetées : 2")),
        () -> assertTrue(rejectedContent.contains("101")),
        () -> assertTrue(rejectedContent.contains("102")));
  }

  @Test
  void shouldUseDvfRejectedIdsForDvfJob() throws Exception {
    when(dvfSkipListener.getIdsRejetes())
        .thenReturn(List.of(DVF_REJECTED_ID_1, DVF_REJECTED_ID_2, DVF_REJECTED_ID_3));

    JobExecution parentExecution = createParentExecution();
    JobExecution jobExecution =
        createJobExecution(
            "importDvfJob",
            BatchStatus.COMPLETED,
            Set.of(
                createStep(
                    "csvToStagingStep", BatchStatus.COMPLETED, DVF_READ_COUNT, DVF_WRITE_COUNT),
                createStep(
                    "masterStepDvf",
                    BatchStatus.COMPLETED,
                    DVF_READ_COUNT,
                    DVF_IMPORT_WRITE_COUNT)));

    when(jobRepository.getJobExecution(PARENT_JOB_ID)).thenReturn(parentExecution);

    listener.afterJob(jobExecution);

    Path mainReport = temporaryDirectory.resolve("rapport_importDvfJob_" + TIMESTAMP + ".txt");

    Path rejectedReport =
        temporaryDirectory.resolve("rapport_rejetes_importDvfJob_" + TIMESTAMP + ".txt");

    String mainContent = Files.readString(mainReport, StandardCharsets.UTF_8);

    String rejectedContent = Files.readString(rejectedReport, StandardCharsets.UTF_8);

    assertAll(
        () -> assertTrue(mainContent.contains("Nombre d'ID rejetés : 3")),
        () -> assertTrue(mainContent.contains("ReadCount  : " + DVF_READ_COUNT)),
        () -> assertTrue(mainContent.contains("WriteCount : " + DVF_IMPORT_WRITE_COUNT)),
        () -> assertTrue(rejectedContent.contains("Nombre de lignes rejetées : 3")),
        () -> assertTrue(rejectedContent.contains(String.valueOf(DVF_REJECTED_ID_1))),
        () -> assertTrue(rejectedContent.contains(String.valueOf(DVF_REJECTED_ID_2))),
        () -> assertTrue(rejectedContent.contains(String.valueOf(DVF_REJECTED_ID_3))));
  }

  @Test
  void shouldWriteNoInputFileMessage() throws Exception {
    JobExecution parentExecution = createParentExecution();

    JobExecution jobExecution =
        createJobExecution("importAdresseJob", BatchStatus.COMPLETED, Set.of());

    when(jobExecution.getJobParameters().getString("lastExitStatus", ""))
        .thenReturn("NO_INPUT_FILE");

    when(jobRepository.getJobExecution(PARENT_JOB_ID)).thenReturn(parentExecution);

    listener.afterJob(jobExecution);

    Path mainReport = temporaryDirectory.resolve("rapport_importAdresseJob_" + TIMESTAMP + ".txt");

    String content = Files.readString(mainReport, StandardCharsets.UTF_8);

    assertAll(
        () -> assertTrue(content.contains("Aucun fichier à traiter")),
        () -> assertFalse(content.contains("=== BILAN DES TEMPS PAR ETAPES ===")),
        () -> assertFalse(content.contains("=== BILAN IMPORT ===")));
  }

  @Test
  void shouldUseCsvErrorAsExitStatusWhenPresent() throws Exception {
    JobExecution parentExecution = createParentExecution();

    JobExecution jobExecution =
        createJobExecution("importAdresseJob", BatchStatus.FAILED, Set.of());

    jobExecution.getExecutionContext().putString("csvStatus", "INVALID_CSV_FORMAT");

    when(jobRepository.getJobExecution(PARENT_JOB_ID)).thenReturn(parentExecution);

    listener.afterJob(jobExecution);

    Path mainReport = temporaryDirectory.resolve("rapport_importAdresseJob_" + TIMESTAMP + ".txt");

    String content = Files.readString(mainReport, StandardCharsets.UTF_8);

    assertTrue(content.contains("ExitStatus : INVALID_CSV_FORMAT"));

    assertFalse(content.contains("ExitStatus : COMPLETED"));
  }

  @Test
  void shouldWriteFailedStepAndItsExceptions() throws Exception {
    RuntimeException failure = new RuntimeException("Erreur pendant l'import");

    StepExecution failedStep =
        createStep(
            "masterStepAdresse",
            BatchStatus.FAILED,
            TEST_FAILED_READ_COUNT,
            TEST_FAILED_WRITE_COUNT);

    when(failedStep.getExitStatus()).thenReturn(new ExitStatus("FAILED", "Erreur fonctionnelle"));

    when(failedStep.getFailureExceptions()).thenReturn(List.of(failure));

    JobExecution parentExecution = createParentExecution();

    JobExecution jobExecution =
        createJobExecution("importAdresseJob", BatchStatus.FAILED, Set.of(failedStep));

    when(jobRepository.getJobExecution(PARENT_JOB_ID)).thenReturn(parentExecution);

    listener.afterJob(jobExecution);

    Path failureReport = temporaryDirectory.resolve("bilan_failed.txt");

    String content = Files.readString(failureReport, StandardCharsets.UTF_8);

    assertAll(
        () -> assertTrue(content.contains("=== BILAN DES ERREURS ===")),
        () -> assertTrue(content.contains("Step : masterStepAdresse")),
        () -> assertTrue(content.contains("Status : FAILED")),
        () ->
            assertTrue(
                content.contains("ExitStatus : exitCode=FAILED") || content.contains("FAILED")),
        () -> assertTrue(content.contains("java.lang.RuntimeException")),
        () -> assertTrue(content.contains("Erreur pendant l'import")));
  }

  @Test
  void shouldIgnoreTechnicalStepsInDurationReport() throws Exception {
    StepExecution ignoredAdresseStep =
        createStep("importAdresseStep", BatchStatus.COMPLETED, TEST_READ_COUNT, TEST_WRITE_COUNT);

    StepExecution ignoredChecksumStep =
        createStep(
            "checksumStep:partition0", BatchStatus.COMPLETED, TEST_READ_COUNT, TEST_WRITE_COUNT);

    StepExecution ignoredDvfStep =
        createStep(
            "importDvfStep:partition0", BatchStatus.COMPLETED, TEST_READ_COUNT, TEST_WRITE_COUNT);

    StepExecution visibleStep =
        createStep("validationStep", BatchStatus.COMPLETED, TEST_READ_COUNT, TEST_WRITE_COUNT);

    JobExecution parentExecution = createParentExecution();

    JobExecution jobExecution =
        createJobExecution(
            "importAdresseJob",
            BatchStatus.COMPLETED,
            Set.of(ignoredAdresseStep, ignoredChecksumStep, ignoredDvfStep, visibleStep));

    when(jobRepository.getJobExecution(PARENT_JOB_ID)).thenReturn(parentExecution);

    listener.afterJob(jobExecution);

    Path mainReport = temporaryDirectory.resolve("rapport_importAdresseJob_" + TIMESTAMP + ".txt");

    String content = Files.readString(mainReport, StandardCharsets.UTF_8);

    assertAll(
        () -> assertTrue(content.contains("validationStep : 5 secondes")),
        () -> assertFalse(content.contains("importAdresseStep:partition0")),
        () -> assertFalse(content.contains("checksumStep:partition0")),
        () -> assertFalse(content.contains("importDvfStep:partition0")));
  }

  @Test
  void shouldThrowExceptionWhenParentJobIdIsMissing() {
    JobExecution jobExecution = mock(JobExecution.class);
    JobParameters parameters = mock(JobParameters.class);

    when(jobExecution.getJobParameters()).thenReturn(parameters);

    when(jobExecution.getExecutionContext()).thenReturn(new ExecutionContext());

    when(parameters.getString("lastExitStatus", "")).thenReturn("");

    when(parameters.getLong("jobExecutionId")).thenReturn(null);

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> listener.afterJob(jobExecution));

    assertEquals("Le paramètre jobExecutionId est absent", exception.getMessage());
  }

  @Test
  void shouldThrowExceptionWhenParentJobDoesNotExist() {
    JobExecution jobExecution =
        createJobExecution("importAdresseJob", BatchStatus.COMPLETED, Set.of());

    when(jobRepository.getJobExecution(PARENT_JOB_ID)).thenReturn(null);

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> listener.afterJob(jobExecution));

    assertEquals("Le job parent est introuvable : " + PARENT_JOB_ID, exception.getMessage());

    verify(jobRepository).getJobExecution(PARENT_JOB_ID);
  }

  private JobExecution createJobExecution(
      String jobName, BatchStatus batchStatus, Set<StepExecution> stepExecutions) {

    JobExecution jobExecution = mock(JobExecution.class);
    JobInstance jobInstance = mock(JobInstance.class);
    JobParameters jobParameters = mock(JobParameters.class);

    ExecutionContext executionContext = new ExecutionContext();

    executionContext.putString("checksum", "checksum-test");

    when(jobExecution.getJobInstance()).thenReturn(jobInstance);

    when(jobInstance.getJobName()).thenReturn(jobName);

    when(jobExecution.getStatus()).thenReturn(batchStatus);

    when(jobExecution.getEndTime()).thenReturn(JOB_END_TIME);

    when(jobExecution.getJobParameters()).thenReturn(jobParameters);

    when(jobParameters.getLong("jobExecutionId")).thenReturn(PARENT_JOB_ID);

    when(jobParameters.getString("lastExitStatus", "")).thenReturn("COMPLETED");

    when(jobExecution.getExecutionContext()).thenReturn(executionContext);

    when(jobExecution.getStepExecutions()).thenReturn(stepExecutions);

    return jobExecution;
  }

  private JobExecution createParentExecution() {
    JobExecution parentExecution = mock(JobExecution.class);

    JobInstance parentJobInstance = mock(JobInstance.class);

    when(parentExecution.getJobInstance()).thenReturn(parentJobInstance);

    when(parentJobInstance.getJobName()).thenReturn("parentJob");

    when(parentExecution.getStartTime()).thenReturn(PARENT_START_TIME);

    return parentExecution;
  }

  private StepExecution createStep(
      String stepName, BatchStatus batchStatus, long readCount, long writeCount) {

    StepExecution stepExecution = mock(StepExecution.class);

    when(stepExecution.getStepName()).thenReturn(stepName);

    when(stepExecution.getStatus()).thenReturn(batchStatus);

    when(stepExecution.getReadCount()).thenReturn(readCount);

    when(stepExecution.getWriteCount()).thenReturn(writeCount);

    when(stepExecution.getStartTime()).thenReturn(STEP_START_TIME);

    when(stepExecution.getEndTime()).thenReturn(STEP_END_TIME);

    when(stepExecution.getExitStatus()).thenReturn(ExitStatus.COMPLETED);

    when(stepExecution.getFailureExceptions()).thenReturn(List.of());

    return stepExecution;
  }
}
