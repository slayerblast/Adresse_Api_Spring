package fr.natsystem.projet.controller;

import fr.natsystem.projet.model.JobStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200/")
@RequestMapping("/api/jobs")
public class BatchController {


    private final JobOperator jobOperator;

    private final JobRepository jobRepository;

    @Qualifier("checkFileJob")
    private final Job checkFileJob;

    @Operation(
            summary = "Lancement du Job",
            description = """
                    Lance le job avec l'innerJob de traitement choisi (si l'innerJob entré est incorrect, celui ci prend importAdresseJob par défaut)
                    - importAdresseJob : job de traitement correspondant aux fichiers de la Base d'Adresse Nationale
                    - importDvfJob : job de traitement correspondant aux fichiers des Demande de Valeurs Foncières
                    - inputFile (Optionnel): il faut rentrer le chemin du fichier en partant de la racine du projet"
                    - Retrieve : Variable True/false placé dans application.properties servant à savoir si il faut télécharger le fichier en ligne si besoin (True) ou non (False)
                    """
    )
    @PostMapping("/batch/lancer/{innerJob}")
    public ResponseEntity<Object> startJob(
            @Parameter(description = """
                    Choisissez entre :
                    - importAdresseJob
                    - importDvfJob
                    """)
            @PathVariable("innerJob") String innerJob,
            @Parameter(description = "Chemin ou nom du fichier à traiter")
            @RequestParam(value = "inputFile", required = false) String inputFile) {

        JobParameters params = new JobParametersBuilder()
                .addString("inputFile", inputFile == null ? "" : inputFile)
                .addLong("startAt", System.currentTimeMillis())
                .addString("innerJob", innerJob)
                .toJobParameters();
        try {
            JobExecution je = jobOperator.start(checkFileJob, params);
            return ResponseEntity.accepted().body(je.getId());
        } catch (Exception e) {
            log.info("job on error");
            return ResponseEntity.internalServerError()
                    .body(e.getMessage());
        }
    }

    @GetMapping("/statut/{jobExecutionId}")
    public ResponseEntity<Object> getJobStatus(@PathVariable("jobExecutionId") Long jobExecutionId) {
        JobExecution je = jobRepository.getJobExecution(jobExecutionId);

        if (je == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(
                new JobStatusResponse(
                        je.getId(),
                        je.getJobInstance().getJobName(),
                        je.getStatus().name(),
                        je.getExitStatus().getExitCode(),
                        je.getExecutionContext().getString("code", ""),
                        je.getExecutionContext().getString("nameCode", "")
                )
        );
    }

}

