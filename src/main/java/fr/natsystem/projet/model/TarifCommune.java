package fr.natsystem.projet.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record TarifCommune(
    String codeInsee,
    BigDecimal prixMoyen,
    BigDecimal prixMedian,
    BigDecimal prixM2,
    Long nombreTransactions,
    BigDecimal prixMoyenN1,
    BigDecimal prixMedianN1,
    BigDecimal prixM2N1,
    Long nombreTransactionsN1,
    BigDecimal variationPrixMoyenPct,
    BigDecimal variationPrixMedianPct,
    BigDecimal variationPrixM2Pct,
    BigDecimal variationNombreTransactionsPct,
    LocalDate dateDebutPeriodeN,
    LocalDate dateFinPeriodeN,
    LocalDate dateDebutPeriodeN1,
    LocalDate dateFinPeriodeN1,
    OffsetDateTime dateCalcul) {}
