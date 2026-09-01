package fr.natsystem.projet.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;

public record ContourCommune(
        String insee,
        String nom,
        BigDecimal prixMoyen,
        String contour
) {}
