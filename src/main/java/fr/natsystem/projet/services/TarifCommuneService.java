package fr.natsystem.projet.services;

import fr.natsystem.projet.model.TarifCommune;
import fr.natsystem.projet.repository.TarifCommuneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TarifCommuneService {
    private final TarifCommuneRepository tarifCommuneRepository;

    public Optional<TarifCommune> findByCodeInsee(
            String codeInsee
    ) {
        return tarifCommuneRepository.findByCodeInsee(codeInsee);
    }

}
