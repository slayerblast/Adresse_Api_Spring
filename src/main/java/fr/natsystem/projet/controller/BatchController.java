package fr.natsystem.projet.controller;

import fr.natsystem.projet.model.JobStatusResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
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


    @PostMapping("/batch/lancer")
    public ResponseEntity<?> startJob() {

        JobParameters params = new JobParametersBuilder()
                .addString("inputFile", "")
                .addLong("startAt", System.currentTimeMillis())
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
    public ResponseEntity<?> getJobStatus(@PathVariable("jobExecutionId") Long jobExecutionId) {
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
                        je.getExecutionContext().getString("code",""),
                        je.getExecutionContext().getString("nameCode","")
                )
        );
    }

}

