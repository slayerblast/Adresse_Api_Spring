package fr.natsystem.projet.testmethods;

import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.natsystem.projet.model.Adresse;
import fr.natsystem.projet.repository.AdresseRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestConstructor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class APIMethodeTest {

  private static final double LATITUDE_INITIALE = 48.8566;
  private static final double LONGITUDE_INITIALE = 2.3522;
  private static final int NOMBRE_POINTS = 100;

  private static final double INCREMENT_COORDONNEES = 0.00001;
  private static final double NANOSECONDS_TO_MILLISECONDS = 1_000_000.0;

  private JobOperator jobLauncher;
  private Job importAdresseJob;
  private final AdresseRepository adresseRepository;

  @Container
  static PostgreSQLContainer<?> postgres =
          new PostgreSQLContainer<>("postgis/postgis:18-3.6")
                  .withInitScript("schema_postgres.sql");;

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @BeforeEach
  void chargerDonnees() throws Exception {

    JobParameters parameters =
            new JobParametersBuilder()
                    .addLong("testRun", System.currentTimeMillis())
                    .addString("inputFile","data/csvFile/adresses-79.csv")
                    .toJobParameters();

    jobLauncher.start(importAdresseJob, parameters);
  }

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
