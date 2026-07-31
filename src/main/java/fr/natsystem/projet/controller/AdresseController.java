package fr.natsystem.projet.controller;

import fr.natsystem.projet.model.Adresse;
import fr.natsystem.projet.services.AdresseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
@Slf4j
@Tag(name = "Adresses", description = "Recherche dans le référentiel local BAN")
@CrossOrigin(origins = "http://localhost:4200/")
@RestController
@RequestMapping("/api/adresses")
@RequiredArgsConstructor
public class AdresseController {

    private final AdresseService service;

    @Operation(
            summary = "Recherche d'adresses",
            description = """
                Recherche une ou plusieurs adresses selon
                le code postal,
                le nom de voie
                et la commune.
                Les critères sont combinables.
                """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Liste paginée des adresses trouvées",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Page.class),
                    examples = @ExampleObject(
                            value = """
                            {
                              "content": [
                                {
                                  "id": "75115_2830_00001",
                                  "numero": "1",
                                  "nom_voie": "Rue du Docteur Finlay",
                                  "code_postal": "75015",
                                  "code_insee": "75115",
                                  "nom_commune": "Paris 15e Arrondissement",
                                  "lat": 48.85286,
                                  "lon": 2.286846
                                }
                              ],
                              "pageable": {
                                "pageNumber": 0,
                                "pageSize": 20
                              },
                              "totalElements": 57,
                              "totalPages": 3,
                              "number": 0
                            }
                            """
                    )
            )
    )
    @GetMapping
    public Page<Adresse> rechercher(
            @Parameter(description = "Code postal (ex : 75015)")
            @RequestParam(required = false) String codePostal,

            @Parameter(description = "Nom de voie (ex : Rue du Docteur Finlay)")
            @RequestParam(required = false) String rue,

            @Parameter(description = "Commune (ex : Paris)")
            @RequestParam(required = false) String commune,

            @PageableDefault(size = 20, page = 0)
            Pageable pageable
    ) {
        log.info("page={}", pageable.getPageNumber());
        log.info("page={}", pageable.getPageSize());
        log.info("offset={}", pageable.getOffset());
        return service.rechercher( codePostal, rue, commune, pageable);
    }
}