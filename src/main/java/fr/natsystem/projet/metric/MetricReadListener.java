package fr.natsystem.projet.metric;

import fr.natsystem.projet.model.Adresse;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.listener.ItemReadListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MetricReadListener implements ItemReadListener<Adresse> {

    private final BatchMetrics metrics;
    private long start;

    @Override
    public void beforeRead() {
        start = System.nanoTime();
    }

    @Override
    public void afterRead(Adresse item) {
        metrics.addReadTime(System.nanoTime() - start);
    }

    @Override
    public void onReadError(Exception ex) {
    }
}
