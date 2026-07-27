package fr.natsystem.projet.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;


public record Adresse(
        @NotBlank String id,
        String id_fantoir,
        @NotBlank String numero,
        String rep,
        @NotBlank String nom_voie,
        String code_postal,
        @NotBlank String code_insee,
        @NotBlank String nom_commune,
        String code_insee_ancienne_commune,
        String nom_ancienne_commune,
        @NotNull double x,
        @NotNull double y,
        @NotNull double lon,
        @NotNull double lat,
        @NotBlank String type_position,
        String alias,
        String nom_ld,
        String libelle_acheminement,
        String nom_afnor,
        @NotBlank String source_position,
        @NotBlank String source_nom_voie,
        @NotNull int certification_commune,
        String cad_parcelles
) {
    private static final String INCONNUE = "inconnue";
    public boolean isBetterThan(Adresse other) {

        boolean better = false;

        if (certification_commune != other.certification_commune) {

            better = certification_commune == 1;

        } else if (!source_nom_voie.equals(other.source_nom_voie)) {

            if (INCONNUE.equalsIgnoreCase(source_nom_voie)) {
                better = false;
            } else if (INCONNUE.equalsIgnoreCase(other.source_nom_voie)) {
                better = true;
            }

        } else if (!source_position.equals(other.source_position)) {

            if (INCONNUE.equalsIgnoreCase(source_position)) {
                better = false;
            } else if (INCONNUE.equalsIgnoreCase(other.source_position)) {
                better = true;
            }
        }

        return better;
    }

    public AdresseKey key() {
        return new AdresseKey(
                id,
                type_position,
                x,
                y
        );
    }


}