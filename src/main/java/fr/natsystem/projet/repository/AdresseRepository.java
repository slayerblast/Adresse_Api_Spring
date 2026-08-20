package fr.natsystem.projet.repository;

import fr.natsystem.projet.model.Adresse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;


@Repository
public interface AdresseRepository {

    Page<Adresse> rechercher(
            String codePostal,
            String rue,
            String commune,
            Pageable pageable
    );

    List<Adresse> autoComplete(String q);


    List<Adresse> findProches(
            @Param("lat") double lat,
            @Param("lon") double lon);
}