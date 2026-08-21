package fr.natsystem.projet.batch.listener;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
@Getter
@Setter
public class BilanJobListener implements JobExecutionListener {
    private final AtomicLong doublon = new AtomicLong(0);
    private final AtomicLong doublonPur = new AtomicLong(0);
    private int obsolete = 0;
    private final AdresseSkipListener skipListener;
    private final JobRepository jobRepository;
    private String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

    @Value("${spring.batch.bilanDir}")
    private String bilanDir;

    @Override
    public void beforeJob(JobExecution je) {
        log.info("Job [{}] demarre",
                je.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution je) {
        String checksum = je.getExecutionContext().getString("checksum","");
        long parentJobId = je.getJobParameters().getLong("jobExecutionId");
        log.info("Job id parent {} ", parentJobId);
        String csvError = je.getExecutionContext().getString("csvStatus", "");
        String status = je.getJobParameters().getString("lastExitStatus");
        String noFile = je.getExecutionContext().getString("noFile", "");
        JobExecution parentExecution = jobRepository.getJobExecution(parentJobId);
        log.info("Job {} : {}", je.getJobInstance().getJobName(), je.getStatus());


        try (FileWriter writer = new FileWriter(bilanDir+"rapport_"+je.getJobInstance().getJobName()+"_"+timestamp+".txt")) {
            Duration jobDuration = Duration.between(
                    parentExecution.getStartTime(),
                    je.getEndTime()
            );


            // ============================
            // BILAN STATUS DU JOB
            // ============================

            writer.write("=== STATUS DU JOB ===\n\n");
            writer.write("Job parent : " + parentExecution.getJobInstance().getJobName() + "\n");
            writer.write("Status : " + je.getStatus() + "\n");
            if (!csvError.isEmpty()) {
                writer.write("ExitStatus : " + csvError + "\n\n");
            } else {
                writer.write("ExitStatus : " + status + "\n\n");
            }
            writer.write("Début : " + parentExecution.getStartTime() + "\n");
            writer.write("Fin    : " + je.getEndTime() + "\n");
            writer.write("Durée totale : " + jobDuration.toSeconds() + " secondes\n\n");


            if (!noFile.equals("Not found") && !status.equals("NO_INPUT_FILE") ) {
                // ============================
                // BILAN du temps par etape
                // ============================
                writer.write("=== BILAN DES TEMPS PAR ETAPES ===\n");
                for (StepExecution step : je.getStepExecutions()) {
                    if (step.getStepName().startsWith("importAdresseStep:") || step.getStepName().startsWith("checksumStep:") ) {
                        continue;
                    }
                    Duration stepDuration = Duration.between(
                            step.getStartTime(),
                            step.getEndTime()
                    );

                    writer.write(
                            step.getStepName()
                                    + " : "
                                    + stepDuration.toSeconds()
                                    + " secondes\n"
                    );
                }
                writer.write("\n");

                // ============================
                // BILAN IMPORT CSV -> STAGING
                // ============================
                StepExecution importSet = je.getStepExecutions().stream()
                        .filter(s -> s.getStepName().equals("csvToStagingStep"))
                        .findFirst().orElse(null);
                if (importSet != null) {
                    writer.write("=== BILAN IMPORT CSV -> STAGING ===\n\n");
                    writer.write("checksum du fichier : " + checksum + "\n\n");
                    //writer.write("ReadCount  : " + importSet.getReadCount() + "\n");
                    //writer.write("WriteCount : " + importSet.getWriteCount() + "\n");
                    //writer.write("Lignes qui n'ont pas passé le BeanValidation : " + importSet.getFilterCount() + "\n");
                    //writer.write("Nombre d'ID rejetés : " + skipListener.getIdsRejetes().size() + "\n\n");

                    // ============================
                    // BILAN IMPORT ADRESSE
                    // ============================

                    importSet = je.getStepExecutions().stream()
                            .filter(s -> s.getStepName().equals("masterStep"))
                            .findFirst().orElse(null);
                    if (importSet != null) {

                        writer.write("=== BILAN IMPORT ===\n\n");
                        writer.write("ReadCount  : " + importSet.getReadCount() + "\n");
                        writer.write("WriteCount : " + importSet.getWriteCount() + "\n");
                        writer.write("Doublons pur : " + doublonPur + "\n");
                        writer.write("Lignes en double : " + doublon + "\n");
                        writer.write("Nombre d'ID rejetés : " + skipListener.getIdsRejetes().size() + "\n\n");
                        writer.write("Lignes obsolète supprimées: " + obsolete + "\n\n");
                    }
                }

            } else {
                writer.write("Aucun fichier à traiter");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // ============================
        // BILAN DES ERREURS
        // ============================

        try (FileWriter writer =
                     new FileWriter(bilanDir+"bilan_failed.txt")) {

            writer.write("=== BILAN DES ERREURS ===\n\n");

            boolean hasFailure = false;

            for (StepExecution step : je.getStepExecutions()) {

                if (step.getStatus() != BatchStatus.FAILED) {
                    continue;
                }

                hasFailure = true;

                writer.write("Step : " + step.getStepName() + "\n");
                writer.write("Status : " + step.getStatus() + "\n");
                writer.write("ExitStatus : " + step.getExitStatus() + "\n\n");

                for (Throwable throwable : step.getFailureExceptions()) {

                    writer.write("Exception :\n");

                    writer.write(
                            throwable.getClass().getName()
                                    + " : "
                                    + throwable.getMessage()
                                    + "\n\n"
                    );
                }

                writer.write(
                        "----------------------------------------\n\n");
            }
            if (!hasFailure) {
                writer.write("Aucune erreur détectée.\n");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        /*
        for (StepExecution stepExecution : je.getStepExecutions()) {

            log.info("Step={} Status={}",
                    stepExecution.getStepName(),
                    stepExecution.getStatus());

            if (!stepExecution.getFailureExceptions().isEmpty()) {

                for (Throwable t : stepExecution.getFailureExceptions()) {

                    log.error(
                            "Erreur sur {}",
                            stepExecution.getStepName(),
                            t);
                }
            }
        }*/
    }
}



