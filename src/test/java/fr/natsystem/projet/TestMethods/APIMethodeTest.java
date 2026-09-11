package fr.natsystem.projet.TestMethods;

import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.natsystem.projet.model.Adresse;
import fr.natsystem.projet.repository.AdresseRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class APIMethodeTest {

  private static final double LATITUDE_INITIALE = 48.8566;
  private static final double LONGITUDE_INITIALE = 2.3522;
  private static final int NOMBRE_POINTS = 100;

  private static final double INCREMENT_COORDONNEES = 0.00001;
  private static final double NANOSECONDS_TO_MILLISECONDS = 1_000_000.0;

  private final AdresseRepository adresseRepository;

  @Test
  void testerTempsReponseFindProchesSur100Points() {

    double latitudeInitiale = LATITUDE_INITIALE;
    double longitudeInitiale = LONGITUDE_INITIALE;
    int nombrePoints = NOMBRE_POINTS;

    // Warm-up
    adresseRepository.findProches(latitudeInitiale, longitudeInitiale);

    long debut = System.nanoTime();

    int nombreResultats = 0;

    for (int i = 0; i < nombrePoints; i++) {

      double lat = latitudeInitiale + (i * INCREMENT_COORDONNEES);
      double lon = longitudeInitiale + (i * INCREMENT_COORDONNEES);

      List<Adresse> adresses = adresseRepository.findProches(lat, lon);

      if (!adresses.isEmpty()) {
        nombreResultats++;
      }
    }

    long fin = System.nanoTime();

    double dureeTotaleMs = (fin - debut) / NANOSECONDS_TO_MILLISECONDS;

    double dureeMoyenneMs = dureeTotaleMs / nombrePoints;

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
        nombrePoints, nombreResultats, dureeTotaleMs, dureeMoyenneMs);
    assertTrue(nombreResultats > 0);
  }
}
