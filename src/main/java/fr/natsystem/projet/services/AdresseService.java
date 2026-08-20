package fr.natsystem.projet.services;

import fr.natsystem.projet.model.Adresse;
import fr.natsystem.projet.repository.AdresseRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.util.List;


@Service
@RequiredArgsConstructor
public class AdresseService {
    private final AdresseRepository repository;

    public Page<Adresse> rechercher(
            String codePostal,
            String rue,
            String commune,
            Pageable pageable
    ) {
        return repository.rechercher(codePostal, rue, commune, pageable);
    }

    public List<Adresse> autoComplete(String q) {
        return repository.autoComplete(q);
    }

    public List<Adresse> trouverAdressesProches(double lat, double lon) {
        return repository.findProches(lat, lon);
    }
}
