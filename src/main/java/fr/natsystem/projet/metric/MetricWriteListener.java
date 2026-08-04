package fr.natsystem.projet.metric;

import fr.natsystem.projet.model.Adresse;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.listener.ItemWriteListener;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MetricWriteListener implements ItemWriteListener<Adresse> {

    private final BatchMetrics metrics;
    private long start;

    @Override
    public void beforeWrite(Chunk<? extends Adresse> items) {
        start = System.nanoTime();
    }

    @Override
    public void afterWrite(Chunk<? extends Adresse> items) {
        metrics.addWriteTime(System.nanoTime() - start);
    }

    @Override
    public void onWriteError(Exception exception,
                             Chunk<? extends Adresse> items) {
    }
}