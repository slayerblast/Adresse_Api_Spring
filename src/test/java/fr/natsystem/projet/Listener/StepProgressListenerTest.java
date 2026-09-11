package fr.natsystem.projet.Listener;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import fr.natsystem.projet.batch.listener.StepProgressListener;
import fr.natsystem.projet.services.AdresseCacheService;
import fr.natsystem.projet.services.DvfCacheService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.file.FlatFileParseException;

class StepProgressListenerTest {

  private AdresseCacheService adresseCacheService;
  private DvfCacheService dvfCacheService;

  private StepProgressListener listener;

  private StepExecution stepExecution;
  private JobExecution jobExecution;
  private JobInstance jobInstance;

  private ExecutionContext stepExecutionContext;
  private ExecutionContext jobExecutionContext;

  @BeforeEach
  void setUp() {
    adresseCacheService = mock(AdresseCacheService.class);
    dvfCacheService = mock(DvfCacheService.class);

    listener = new StepProgressListener(adresseCacheService, dvfCacheService);

    stepExecution = mock(StepExecution.class);
    jobExecution = mock(JobExecution.class);
    jobInstance = mock(JobInstance.class);

    stepExecutionContext = new ExecutionContext();
    jobExecutionContext = new ExecutionContext();

    stepExecutionContext.putString("codeInsee", "94046");

    when(stepExecution.getExecutionContext()).thenReturn(stepExecutionContext);

    when(stepExecution.getJobExecution()).thenReturn(jobExecution);

    when(jobExecution.getJobInstance()).thenReturn(jobInstance);

    when(jobExecution.getExecutionContext()).thenReturn(jobExecutionContext);
  }

  @Test
  void shouldLoadAdresseCacheBeforeAdresseStep() {
    when(jobInstance.getJobName()).thenReturn("importAdresseJob");

    listener.beforeStep(stepExecution);

    verify(adresseCacheService).load("94046");
    verifyNoInteractions(dvfCacheService);
  }

  @Test
  void shouldLoadDvfCacheBeforeDvfStep() {
    when(jobInstance.getJobName()).thenReturn("importDvfJob");

    listener.beforeStep(stepExecution);

    verify(dvfCacheService).load("94046");
    verifyNoInteractions(adresseCacheService);
  }

  @Test
  void shouldLoadAdresseCacheForUnknownJob() {
    when(jobInstance.getJobName()).thenReturn("unknownJob");

    listener.beforeStep(stepExecution);

    verify(adresseCacheService).load("94046");
    verifyNoInteractions(dvfCacheService);
  }

  @Test
  void shouldThrowExceptionWhenCodeInseeIsAbsent() {
    when(jobInstance.getJobName()).thenReturn("importAdresseJob");

    stepExecutionContext.remove("codeInsee");

    assertThrows(ClassCastException.class, () -> listener.beforeStep(stepExecution));

    verifyNoInteractions(adresseCacheService);
    verifyNoInteractions(dvfCacheService);
  }

  @Test
  void shouldReturnInvalidCsvWhenFlatFileParseExceptionIsPresent() {
    FlatFileParseException parseException = mock(FlatFileParseException.class);

    when(stepExecution.getFailureExceptions()).thenReturn(List.of(parseException));

    ExitStatus result = listener.afterStep(stepExecution);

    assertEquals("INVALID_CSV", result.getExitCode());

    assertEquals("INVALID_CSV", jobExecutionContext.getString("csvStatus"));
  }

  @Test
  void shouldReturnInvalidCsvWhenFlatFileParseExceptionIsNested() {
    FlatFileParseException parseException = mock(FlatFileParseException.class);

    RuntimeException firstWrapper = new RuntimeException("Erreur intermédiaire", parseException);

    IllegalStateException secondWrapper = new IllegalStateException("Erreur globale", firstWrapper);

    when(stepExecution.getFailureExceptions()).thenReturn(List.of(secondWrapper));

    ExitStatus result = listener.afterStep(stepExecution);

    assertEquals("INVALID_CSV", result.getExitCode());

    assertEquals("INVALID_CSV", jobExecutionContext.getString("csvStatus"));
  }

  @Test
  void shouldReturnCurrentExitStatusWhenThereIsNoFailure() {
    ExitStatus originalExitStatus = ExitStatus.COMPLETED;

    when(stepExecution.getFailureExceptions()).thenReturn(List.of());

    when(stepExecution.getExitStatus()).thenReturn(originalExitStatus);

    ExitStatus result = listener.afterStep(stepExecution);

    assertSame(originalExitStatus, result);

    assertEquals("", jobExecutionContext.getString("csvStatus", ""));
  }

  @Test
  void shouldReturnCurrentExitStatusForNonCsvFailure() {
    ExitStatus originalExitStatus = ExitStatus.FAILED;

    RuntimeException failure = new RuntimeException("Erreur technique");

    when(stepExecution.getFailureExceptions()).thenReturn(List.of(failure));

    when(stepExecution.getExitStatus()).thenReturn(originalExitStatus);

    ExitStatus result = listener.afterStep(stepExecution);

    assertSame(originalExitStatus, result);

    assertEquals("", jobExecutionContext.getString("csvStatus", ""));
  }

  @Test
  void shouldFindCsvExceptionAmongSeveralFailures() {
    RuntimeException firstFailure = new RuntimeException("Première erreur");

    FlatFileParseException parseException = mock(FlatFileParseException.class);

    RuntimeException csvFailure = new RuntimeException("Erreur de lecture du CSV", parseException);

    when(stepExecution.getFailureExceptions()).thenReturn(List.of(firstFailure, csvFailure));

    ExitStatus result = listener.afterStep(stepExecution);

    assertEquals("INVALID_CSV", result.getExitCode());

    assertEquals("INVALID_CSV", jobExecutionContext.getString("csvStatus"));

    verify(stepExecution, never()).getExitStatus();
  }
}
