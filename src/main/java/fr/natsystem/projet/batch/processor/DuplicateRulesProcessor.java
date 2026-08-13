package fr.natsystem.projet.batch.processor;

import fr.natsystem.projet.batch.listener.BilanJobListener;
import fr.natsystem.projet.model.Adresse;
import fr.natsystem.projet.model.AdresseKey;
import fr.natsystem.projet.services.AdresseCacheService;
import fr.natsystem.projet.metric.BatchMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class DuplicateRulesProcessor
        implements ItemProcessor<Adresse, Adresse>{

    private final AdresseCacheService adresseCacheService;
    private final BilanJobListener bilanJobListener;
    private final BatchMetrics metrics;

    @Override
    public @Nullable Adresse process(Adresse item) {

        long start = System.nanoTime();

        try {

            AdresseKey key = item.key();
            Adresse existing = adresseCacheService.get(key);

            if (existing == null) {

                adresseCacheService.put(key, item);

            } else if (existing.equals(item)) {

                bilanJobListener.getDoublonPur().incrementAndGet();

                item = null;

            } else if (item.isBetterThan(existing)) {

                adresseCacheService.put(key, item);
                bilanJobListener.getDoublon().incrementAndGet();

            } else {
                bilanJobListener.getDoublon().incrementAndGet();
                item = null;
            }

            return item;

        } finally {

            metrics.addProcessorTime(
                    System.nanoTime() - start);
        }
    }
}

