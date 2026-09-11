package fr.natsystem.projet.batch.listener;

import fr.natsystem.projet.model.Dvf;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.stereotype.Component;

@Slf4j
@Getter
@Component
public class DvfSkipListener implements SkipListener<Dvf, Dvf> {
  private final List<Long> skippedIds = new ArrayList<>();
  private final List<Long> idsRejetes = new ArrayList<>();

  @Override
  public void onSkipInRead(Throwable t) {
    log.warn("Skip lecture : {}", t.getMessage());
  }

  @Override
  public void onSkipInWrite(Dvf item, Throwable t) {
    log.warn("Skip write id={}", item.id());
    skippedIds.add(item.id());
  }

  @Override
  public void onSkipInProcess(Dvf item, Throwable t) {
    log.warn("Skip process id={}", item.id());
    idsRejetes.add(item.id());
  }
}
