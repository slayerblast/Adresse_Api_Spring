package fr.natsystem.projet.batch.listener;

import fr.natsystem.projet.model.Adresse;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.stereotype.Component;

@Slf4j
@Getter
@Component
public class AdresseSkipListener implements SkipListener<Adresse, Adresse> {

  private final List<String> skippedIds = new ArrayList<>();
  private final List<String> idsRejetes = new ArrayList<>();

  @Override
  public void onSkipInRead(Throwable t) {
    log.warn("Skip lecture : {}", t.getMessage());
  }

  @Override
  public void onSkipInProcess(Adresse adresse, Throwable t) {
    log.warn("Skip process id={}", adresse.id());
    idsRejetes.add(adresse.id());
  }

  @Override
  public void onSkipInWrite(Adresse adresse, Throwable t) {
    log.warn("Skip write id={}", adresse.id());
    skippedIds.add(adresse.id());
  }
}
