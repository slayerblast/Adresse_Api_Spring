package fr.natsystem.projet.batch.listener;

import fr.natsystem.projet.model.Adresse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.ChunkListener;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StepListeners implements StepExecutionListener, ChunkListener, SkipListener {

  private final StepProgressListener progressListener;
  private final AdresseSkipListener skipListener;
  private final ChunkListener metricChunkListener;
  private final NestedJobStepListener nestedJobStepListener;

  @Override
  public void beforeStep(StepExecution stepExecution) {
    progressListener.beforeStep(stepExecution);
    nestedJobStepListener.beforeStep(stepExecution);
  }

  @Override
  public ExitStatus afterStep(StepExecution stepExecution) {
    ExitStatus status = nestedJobStepListener.afterStep(stepExecution);
    ExitStatus progressStatus = progressListener.afterStep(stepExecution);

    return status != null ? status : progressStatus;
  }

  @Override
  public void beforeChunk(Chunk context) {
    metricChunkListener.beforeChunk(context);
  }

  @Override
  public void afterChunk(Chunk context) {
    metricChunkListener.afterChunk(context);
  }

  @Override
  public void onSkipInRead(Throwable t) {
    skipListener.onSkipInRead(t);
  }

  public void onSkipInProcess(Adresse adresse, Throwable t) {
    skipListener.onSkipInProcess(adresse, t);
  }

  public void onSkipInWrite(Adresse adresse, Throwable t) {
    skipListener.onSkipInWrite(adresse, t);
  }
}
