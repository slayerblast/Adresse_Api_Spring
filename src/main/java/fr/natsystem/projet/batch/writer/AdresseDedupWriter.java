package fr.natsystem.projet.batch.writer;

import fr.natsystem.projet.model.Adresse;
import fr.natsystem.projet.services.AdresseCacheService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class AdresseDedupWriter implements ItemWriter<Adresse> {

  private final AdresseCacheService adresseCacheService;
  private final JdbcBatchItemWriter<Adresse> delegate;

  @Override
  public void write(Chunk<? extends Adresse> chunk) throws Exception {

    List<Adresse> uniques = new ArrayList<>();

    for (Adresse adresse : chunk) {

      Adresse current = adresseCacheService.get(adresse.key());

      if (current == adresse) {
        uniques.add(adresse);
      }
    }

    if (!uniques.isEmpty()) {
      delegate.write(new Chunk<>(uniques));
    }
  }
}
