package fr.natsystem.projet.services;

import fr.natsystem.projet.model.ContourCommune;
import fr.natsystem.projet.repository.ContourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContourCommuneService {
    private final ContourRepository contourRepository;

    public List<ContourCommune> findAll(){
        return contourRepository.findAll();
    }
}
