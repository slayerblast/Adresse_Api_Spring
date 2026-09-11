package fr.natsystem.projet.batch.listener;

import fr.natsystem.projet.services.AdresseCacheService;
import fr.natsystem.projet.services.DvfCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.file.FlatFileParseException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StepProgressListener implements StepExecutionListener {
  private final AdresseCacheService adresseCacheService;
  private final DvfCacheService dvfCacheService;

  @Override
  public void beforeStep(StepExecution s) {

    String codeInsee = s.getExecutionContext().getString("codeInsee");

    if (s.getJobExecution().getJobInstance().getJobName().equals("importAdresseJob")) {

      adresseCacheService.load(codeInsee);
    } else if (s.getJobExecution().getJobInstance().getJobName().equals("importDvfJob")) {

      dvfCacheService.load(codeInsee);
    } else {

      adresseCacheService.load(codeInsee);
    }
  }

  @Override
  public ExitStatus afterStep(StepExecution s) {
    for (Throwable cause : s.getFailureExceptions()) {
      if (containsCause(cause, FlatFileParseException.class)) {
        s.getJobExecution().getExecutionContext().putString("csvStatus", "INVALID_CSV");
        return new ExitStatus("INVALID_CSV");
      }
    }
    return s.getExitStatus();
  }

  private boolean containsCause(Throwable cause, Class<? extends Throwable> cls) {
    while (cause != null) {
      if (cls.isInstance(cause)) {
        return true;
      }
      cause = cause.getCause();
    }
    return false;
  }
}
