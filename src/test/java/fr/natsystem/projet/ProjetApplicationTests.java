package fr.natsystem.projet;

import fr.natsystem.projet.model.Adresse;
import fr.natsystem.projet.repository.AdresseRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;

import java.util.List;

@SpringBootTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class ProjetApplicationTests {

    private final AdresseRepository adresseRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void testerTempsReponseFindProchesSur100Points() {

        double latitudeInitiale = 48.8566;
        double longitudeInitiale = 2.3522;
        int nombrePoints = 100;

        // Warm-up
        adresseRepository.findProches(
                latitudeInitiale,
                longitudeInitiale
        );

        long debut = System.nanoTime();

        int nombreResultats = 0;

        for (int i = 0; i < nombrePoints; i++) {

            double lat = latitudeInitiale + (i * 0.00001);
            double lon = longitudeInitiale + (i * 0.00001);

            List<Adresse> adresses =
                    adresseRepository.findProches(lat, lon);

            if (!adresses.isEmpty()) {
                nombreResultats++;
            }
        }

        long fin = System.nanoTime();

        double dureeTotaleMs =
                (fin - debut) / 1_000_000.0;

        double dureeMoyenneMs =
                dureeTotaleMs / nombrePoints;

        System.out.printf(
                """
                
                ===== TEST POSTGIS =====
                Nombre de points       : %d
                Points avec résultat   : %d
                Durée totale           : %.2f ms
                Durée moyenne          : %.2f ms
                ========================
                %n
                """,
                nombrePoints,
                nombreResultats,
                dureeTotaleMs,
                dureeMoyenneMs
        );
    }
}

