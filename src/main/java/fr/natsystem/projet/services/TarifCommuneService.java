package fr.natsystem.projet.services;

import fr.natsystem.projet.model.TarifCommune;
import fr.natsystem.projet.repository.TarifCommuneRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TarifCommuneService {
  private final TarifCommuneRepository tarifCommuneRepository;

  public Optional<TarifCommune> findByCodeInsee(String codeInsee) {
    return tarifCommuneRepository.findByCodeInsee(codeInsee);
  }
}
