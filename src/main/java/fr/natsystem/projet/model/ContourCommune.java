package fr.natsystem.projet.model;

import jakarta.persistence.Column;

import java.math.BigDecimal;

public record ContourCommune(
        String insee,
        String nom,
        @Column(name="prix_m2")
        BigDecimal prixM2,
        String contour
) {}
