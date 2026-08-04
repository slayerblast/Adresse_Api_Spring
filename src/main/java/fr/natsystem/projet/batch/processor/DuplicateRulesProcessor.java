package fr.natsystem.projet.batch.processor;

import fr.natsystem.projet.batch.listener.BilanJobListener;
import fr.natsystem.projet.model.Adresse;
import fr.natsystem.projet.model.AdresseKey;
import fr.natsystem.projet.services.AdresseCacheService;
import fr.natsystem.projet.metric.BatchMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DuplicateRulesProcessor
        implements ItemProcessor<Adresse, Adresse> {

    private final AdresseCacheService adresseCacheService;
    private final BilanJobListener bilanJobListener;
    private final BatchMetrics metrics;

    @Override
    public @Nullable Adresse process(Adresse item) {

        long start = System.nanoTime();

        try {

            // Si on change de commune, on recharge le cache
            if (!item.code_insee()
                    .equals(adresseCacheService.getCurrentCodeInsee())) {

                adresseCacheService.load(item.code_insee());
            }

            AdresseKey key = item.key();
            Adresse existing = adresseCacheService.get(key);

            if (existing == null) {

                adresseCacheService.put(key, item);

            } else if (existing.equals(item)) {

                bilanJobListener.setDoublonPur(
                        bilanJobListener.getDoublonPur() + 1);

                item = null;

            } else if (item.isBetterThan(existing)) {

                adresseCacheService.put(key, item);

                bilanJobListener.setDoublon(
                        bilanJobListener.getDoublon() + 1);

            } else {

                bilanJobListener.setDoublon(
                        bilanJobListener.getDoublon() + 1);

                item = null;
            }

            return item;

        } finally {

            metrics.addProcessorTime(
                    System.nanoTime() - start);
        }
    }
}

