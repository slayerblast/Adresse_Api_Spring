package fr.natsystem.projet.Listener;

import static org.junit.jupiter.api.Assertions.assertEquals;

import fr.natsystem.projet.batch.listener.NestedJobStepListener;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.test.MetaDataInstanceFactory;

class NestedJobStepListenerTest {

  private final NestedJobStepListener listener = new NestedJobStepListener();

  @Test
  void shouldSetDefaultCodeWhenNoExceptionExists() {
    StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
    stepExecution.setExitStatus(ExitStatus.COMPLETED);

    ExitStatus result = listener.afterStep(stepExecution);

    assertEquals(ExitStatus.COMPLETED, result);
    assertEquals("202", stepExecution.getJobExecution().getExecutionContext().getString("code"));
    assertEquals(
        "Nouvelle exécution acceptée et démarrée",
        stepExecution.getJobExecution().getExecutionContext().getString("nameCode"));
  }

  @Test
  void shouldSet409CodeWhenJobInstanceAlreadyCompleteExceptionExists() {
    StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();

    stepExecution
        .getJobExecution()
        .addFailureException(new JobInstanceAlreadyCompleteException("already complete"));

    listener.afterStep(stepExecution);

    assertEquals("409", stepExecution.getJobExecution().getExecutionContext().getString("code"));
    assertEquals(
        "JobInstanceAlreadyCompleteException",
        stepExecution.getJobExecution().getExecutionContext().getString("nameCode"));
  }

  @Test
  void shouldSet432CodeWhenJobExecutionAlreadyRunningExceptionExists() {
    StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();

    stepExecution
        .getJobExecution()
        .addFailureException(new JobExecutionAlreadyRunningException("already running"));

    listener.afterStep(stepExecution);

    assertEquals("432", stepExecution.getJobExecution().getExecutionContext().getString("code"));
    assertEquals(
        "JobExecutionAlreadyRunningException",
        stepExecution.getJobExecution().getExecutionContext().getString("nameCode"));
  }
}
