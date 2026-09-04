package fr.natsystem.projet.Repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import fr.natsystem.projet.model.TarifCommune;
import fr.natsystem.projet.repository.TarifCommuneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;


@ExtendWith(MockitoExtension.class)
class TarifCommuneRepositoryTest {

    private static final String CODE_INSEE = "94046";

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ResultSet resultSet;

    private TarifCommuneRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TarifCommuneRepository(jdbcTemplate);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldFindTarifCommuneByCodeInsee() throws Exception {
        BigDecimal prixMoyen = new BigDecimal("450000.50");
        BigDecimal prixMedian = new BigDecimal("420000.00");
        BigDecimal prixM2 = new BigDecimal("6500.75");
        Long nombreTransactions = 125L;

        BigDecimal prixMoyenN1 = new BigDecimal("430000.00");
        BigDecimal prixMedianN1 = new BigDecimal("400000.00");
        BigDecimal prixM2N1 = new BigDecimal("6200.50");
        Long nombreTransactionsN1 = 110L;

        BigDecimal variationPrixMoyen =
                new BigDecimal("4.65");

        BigDecimal variationPrixMedian =
                new BigDecimal("5.00");

        BigDecimal variationPrixM2 =
                new BigDecimal("4.84");

        BigDecimal variationNombreTransactions =
                new BigDecimal("13.64");

        LocalDate dateDebutPeriodeN =
                LocalDate.of(2025, 1, 1);

        LocalDate dateFinPeriodeN =
                LocalDate.of(2025, 12, 31);

        LocalDate dateDebutPeriodeN1 =
                LocalDate.of(2024, 1, 1);

        LocalDate dateFinPeriodeN1 =
                LocalDate.of(2024, 12, 31);

        OffsetDateTime dateCalcul = OffsetDateTime.of(
                2026,
                9,
                4,
                14,
                30,
                0,
                0,
                ZoneOffset.UTC
        );

        configureResultSet(
                prixMoyen,
                prixMedian,
                prixM2,
                nombreTransactions,
                prixMoyenN1,
                prixMedianN1,
                prixM2N1,
                nombreTransactionsN1,
                variationPrixMoyen,
                variationPrixMedian,
                variationPrixM2,
                variationNombreTransactions,
                dateDebutPeriodeN,
                dateFinPeriodeN,
                dateDebutPeriodeN1,
                dateFinPeriodeN1,
                dateCalcul
        );

        when(jdbcTemplate.query(
                anyString(),
                any(RowMapper.class),
                eq(CODE_INSEE)
        )).thenAnswer(invocation -> {
            RowMapper<TarifCommune> rowMapper =
                    invocation.getArgument(1);

            TarifCommune tarifCommune =
                    rowMapper.mapRow(resultSet, 0);

            return List.of(tarifCommune);
        });

        Optional<TarifCommune> result =
                repository.findByCodeInsee(CODE_INSEE);

        assertTrue(result.isPresent());

        TarifCommune expected = new TarifCommune(
                CODE_INSEE,

                prixMoyen,
                prixMedian,
                prixM2,
                nombreTransactions,

                prixMoyenN1,
                prixMedianN1,
                prixM2N1,
                nombreTransactionsN1,

                variationPrixMoyen,
                variationPrixMedian,
                variationPrixM2,
                variationNombreTransactions,

                dateDebutPeriodeN,
                dateFinPeriodeN,
                dateDebutPeriodeN1,
                dateFinPeriodeN1,

                dateCalcul
        );

        assertEquals(expected, result.orElseThrow());

        verify(jdbcTemplate).query(
                anyString(),
                any(RowMapper.class),
                eq(CODE_INSEE)
        );

        verifyResultSetAccess();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnEmptyWhenCodeInseeDoesNotExist() {
        when(jdbcTemplate.query(
                anyString(),
                any(RowMapper.class),
                eq("99999")
        )).thenReturn(List.of());

        Optional<TarifCommune> result =
                repository.findByCodeInsee("99999");

        assertTrue(result.isEmpty());

        verify(jdbcTemplate).query(
                anyString(),
                any(RowMapper.class),
                eq("99999")
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnOnlyFirstTarifCommune() {
        TarifCommune firstTarif = createTarifCommune(
                "94046"
        );

        TarifCommune secondTarif = createTarifCommune(
                "94047"
        );

        when(jdbcTemplate.query(
                anyString(),
                any(RowMapper.class),
                eq(CODE_INSEE)
        )).thenReturn(List.of(firstTarif, secondTarif));

        Optional<TarifCommune> result =
                repository.findByCodeInsee(CODE_INSEE);

        assertTrue(result.isPresent());
        assertEquals(firstTarif, result.orElseThrow());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldUseCodeInseeInSqlQuery() {
        when(jdbcTemplate.query(
                anyString(),
                any(RowMapper.class),
                eq(CODE_INSEE)
        )).thenReturn(List.of());

        Optional<TarifCommune> result =
                repository.findByCodeInsee(CODE_INSEE);

        assertFalse(result.isPresent());

        verify(jdbcTemplate).query(
                anyString(),
                any(RowMapper.class),
                eq(CODE_INSEE)
        );
    }

    private void configureResultSet(
            BigDecimal prixMoyen,
            BigDecimal prixMedian,
            BigDecimal prixM2,
            Long nombreTransactions,
            BigDecimal prixMoyenN1,
            BigDecimal prixMedianN1,
            BigDecimal prixM2N1,
            Long nombreTransactionsN1,
            BigDecimal variationPrixMoyen,
            BigDecimal variationPrixMedian,
            BigDecimal variationPrixM2,
            BigDecimal variationNombreTransactions,
            LocalDate dateDebutPeriodeN,
            LocalDate dateFinPeriodeN,
            LocalDate dateDebutPeriodeN1,
            LocalDate dateFinPeriodeN1,
            OffsetDateTime dateCalcul
    ) throws Exception {

        when(resultSet.getString("code_insee"))
                .thenReturn(CODE_INSEE);

        when(resultSet.getBigDecimal("prix_moyen"))
                .thenReturn(prixMoyen);

        when(resultSet.getBigDecimal("prix_median"))
                .thenReturn(prixMedian);

        when(resultSet.getBigDecimal("prix_m2"))
                .thenReturn(prixM2);

        when(resultSet.getObject(
                "nombre_transactions",
                Long.class
        )).thenReturn(nombreTransactions);

        when(resultSet.getBigDecimal("prix_moyen_n_1"))
                .thenReturn(prixMoyenN1);

        when(resultSet.getBigDecimal("prix_median_n_1"))
                .thenReturn(prixMedianN1);

        when(resultSet.getBigDecimal("prix_m2_n_1"))
                .thenReturn(prixM2N1);

        when(resultSet.getObject(
                "nombre_transactions_n_1",
                Long.class
        )).thenReturn(nombreTransactionsN1);

        when(resultSet.getBigDecimal(
                "variation_prix_moyen_pct"
        )).thenReturn(variationPrixMoyen);

        when(resultSet.getBigDecimal(
                "variation_prix_median_pct"
        )).thenReturn(variationPrixMedian);

        when(resultSet.getBigDecimal(
                "variation_prix_m2_pct"
        )).thenReturn(variationPrixM2);

        when(resultSet.getBigDecimal(
                "variation_nombre_transactions_pct"
        )).thenReturn(variationNombreTransactions);

        when(resultSet.getObject(
                "date_debut_periode_n",
                LocalDate.class
        )).thenReturn(dateDebutPeriodeN);

        when(resultSet.getObject(
                "date_fin_periode_n",
                LocalDate.class
        )).thenReturn(dateFinPeriodeN);

        when(resultSet.getObject(
                "date_debut_periode_n_1",
                LocalDate.class
        )).thenReturn(dateDebutPeriodeN1);

        when(resultSet.getObject(
                "date_fin_periode_n_1",
                LocalDate.class
        )).thenReturn(dateFinPeriodeN1);

        when(resultSet.getObject(
                "date_calcul",
                OffsetDateTime.class
        )).thenReturn(dateCalcul);
    }

    private void verifyResultSetAccess() throws Exception {
        verify(resultSet).getString("code_insee");

        verify(resultSet).getBigDecimal("prix_moyen");
        verify(resultSet).getBigDecimal("prix_median");
        verify(resultSet).getBigDecimal("prix_m2");

        verify(resultSet).getObject(
                "nombre_transactions",
                Long.class
        );

        verify(resultSet).getBigDecimal("prix_moyen_n_1");
        verify(resultSet).getBigDecimal("prix_median_n_1");
        verify(resultSet).getBigDecimal("prix_m2_n_1");

        verify(resultSet).getObject(
                "nombre_transactions_n_1",
                Long.class
        );

        verify(resultSet).getBigDecimal(
                "variation_prix_moyen_pct"
        );

        verify(resultSet).getBigDecimal(
                "variation_prix_median_pct"
        );

        verify(resultSet).getBigDecimal(
                "variation_prix_m2_pct"
        );

        verify(resultSet).getBigDecimal(
                "variation_nombre_transactions_pct"
        );

        verify(resultSet).getObject(
                "date_debut_periode_n",
                LocalDate.class
        );

        verify(resultSet).getObject(
                "date_fin_periode_n",
                LocalDate.class
        );

        verify(resultSet).getObject(
                "date_debut_periode_n_1",
                LocalDate.class
        );

        verify(resultSet).getObject(
                "date_fin_periode_n_1",
                LocalDate.class
        );

        verify(resultSet).getObject(
                "date_calcul",
                OffsetDateTime.class
        );
    }

    private TarifCommune createTarifCommune(
            String codeInsee
    ) {
        return new TarifCommune(
                codeInsee,

                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0L,

                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0L,

                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,

                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),

                OffsetDateTime.of(
                        2026,
                        9,
                        4,
                        14,
                        30,
                        0,
                        0,
                        ZoneOffset.UTC
                )
        );
    }
}