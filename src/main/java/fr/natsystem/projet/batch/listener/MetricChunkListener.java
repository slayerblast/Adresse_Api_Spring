package fr.natsystem.projet.batch.listener;

import fr.natsystem.projet.services.BatchMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.listener.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricChunkListener implements ChunkListener {

    private final BatchMetrics metrics;

    private long start;

    @Override
    public void beforeChunk(Chunk chunk) {
        start = System.currentTimeMillis();
    }

    @Override
    public void afterChunk(Chunk chunk) {

        log.info(
                "chunk={}ms processor={}ms",
                System.currentTimeMillis() - start,
                metrics.consumeProcessorMs()
        );
    }
}