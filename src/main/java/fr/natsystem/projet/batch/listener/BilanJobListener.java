package fr.natsystem.projet.batch.listener;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
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
    private final DvfSkipListener dvfSkipListener;
    private final JobRepository jobRepository;
    private String timestamp = ZonedDateTime.now(ZoneId.of("Europe/Paris")).format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

    @Value("${spring.batch.bilanDir}")
    private String bilanDir;

    @Override
    public void beforeJob(JobExecution je) {
        log.info("Job [{}] demarre",
                je.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String checksum = jobExecution.getExecutionContext()
                .getString("checksum", "");

        String csvError = jobExecution.getExecutionContext()
                .getString("csvStatus", "");

        String noFile = jobExecution.getExecutionContext()
                .getString("noFile", "");

        String status = jobExecution.getJobParameters()
                .getString("lastExitStatus", "");

        Long parentJobId = jobExecution.getJobParameters()
                .getLong("jobExecutionId");

        if (parentJobId == null) {
            throw new IllegalStateException(
                    "Le paramètre jobExecutionId est absent"
            );
        }

        JobExecution parentExecution =
                jobRepository.getJobExecution(parentJobId);

        if (parentExecution == null) {
            throw new IllegalStateException(
                    "Le job parent est introuvable : " + parentJobId
            );
        }

        log.info("Job id parent {}", parentJobId);
        log.info(
                "Job {} : {}",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getStatus()
        );

        writeMainReport(
                jobExecution,
                parentExecution,
                checksum,
                csvError,
                status,
                noFile,
                timestamp
        );

        writeFailureReport(jobExecution);
        writeRejectedReport(jobExecution,timestamp);
    }

    private void writeMainReport(
            JobExecution jobExecution,
            JobExecution parentExecution,
            String checksum,
            String csvError,
            String status,
            String noFile,
            String timestamp
    ) {
        String jobName = jobExecution.getJobInstance().getJobName();

        Path reportPath = Path.of(
                bilanDir,
                "rapport_" + jobName + "_" + timestamp + ".txt"
        );

        try (BufferedWriter writer = Files.newBufferedWriter(
                reportPath,
                StandardCharsets.UTF_8
        )) {
            Duration jobDuration = calculateDuration(
                    parentExecution.getStartTime(),
                    jobExecution.getEndTime()
            );

            writer.write("=== STATUS DU JOB ===\n\n");
            writer.write(
                    "Job parent : "
                            + parentExecution.getJobInstance().getJobName()
                            + "\n"
            );
            writer.write("Status : " + jobExecution.getStatus() + "\n");

            String exitStatus = csvError.isBlank() ? status : csvError;
            writer.write("ExitStatus : " + exitStatus + "\n\n");

            writer.write("Début : " + parentExecution.getStartTime() + "\n");
            writer.write("Fin   : " + jobExecution.getEndTime() + "\n");
            writer.write(
                    "Durée totale : "
                            + jobDuration.toSeconds()
                            + " secondes\n\n"
            );

            if ("Not found".equals(noFile)
                    || "NO_INPUT_FILE".equals(status)) {
                writer.write("Aucun fichier à traiter\n");
                return;
            }

            writeStepDurations(writer, jobExecution);
            writeImportReport(writer, jobExecution, checksum);

        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Impossible d'écrire le rapport principal : " + reportPath,
                    e
            );
        }
    }
    private void writeStepDurations(
            BufferedWriter writer,
            JobExecution jobExecution
    ) throws IOException {

        writer.write("=== BILAN DES TEMPS PAR ETAPES ===\n");

        for (StepExecution step : jobExecution.getStepExecutions()) {
            String stepName = step.getStepName();

            boolean ignored = stepName.startsWith("importAdresseStep:")
                    || stepName.startsWith("checksumStep:")
                    || stepName.startsWith("importDvfStep:");

            if (ignored) {
                continue;
            }

            Duration duration = calculateDuration(
                    step.getStartTime(),
                    step.getEndTime()
            );

            writer.write(
                    "%s : %d secondes%n"
                            .formatted(stepName, duration.toSeconds())
            );
        }

        writer.newLine();
    }
    private void writeImportReport(
            BufferedWriter writer,
            JobExecution jobExecution,
            String checksum
    ) throws IOException {

        boolean hasCsvImport = jobExecution.getStepExecutions()
                .stream()
                .anyMatch(step ->
                        "csvToStagingStep".equals(step.getStepName())
                );

        if (!hasCsvImport) {
            return;
        }

        writer.write("=== BILAN IMPORT CSV -> STAGING ===\n\n");
        writer.write("Checksum du fichier : " + checksum + "\n\n");

        StepExecution importStep = jobExecution.getStepExecutions()
                .stream()
                .filter(step ->
                        "masterStepAdresse".equals(step.getStepName())
                                || "masterStepDvf".equals(step.getStepName())
                )
                .findFirst()
                .orElse(null);

        if (importStep == null) {
            return;
        }

        String jobName = jobExecution.getJobInstance().getJobName();

        int rejectedCount = "importDvfJob".equals(jobName)
                ? dvfSkipListener.getIdsRejetes().size()
                : skipListener.getIdsRejetes().size();

        writer.write("=== BILAN IMPORT ===\n\n");
        writer.write("ReadCount  : " + importStep.getReadCount() + "\n");
        writer.write("WriteCount : " + importStep.getWriteCount() + "\n");
        writer.write("Doublons purs : " + doublonPur + "\n");
        writer.write("Lignes en double : " + doublon + "\n");
        writer.write("Nombre d'ID rejetés : " + rejectedCount + "\n\n");
        writer.write(
                "Lignes obsolètes supprimées : " + obsolete + "\n\n"
        );
    }
    private Duration calculateDuration(
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        if (startTime == null || endTime == null) {
            return Duration.ZERO;
        }

        ZoneId zoneId = ZoneId.systemDefault();

        return Duration.between(
                startTime.atZone(zoneId).toInstant(),
                endTime.atZone(zoneId).toInstant()
        );
    }
    private void writeFailureReport(JobExecution jobExecution) {
        Path reportPath = Path.of(bilanDir, "bilan_failed.txt");

        List<StepExecution> failedSteps = jobExecution.getStepExecutions()
                .stream()
                .filter(step -> step.getStatus() == BatchStatus.FAILED)
                .toList();

        try (BufferedWriter writer = Files.newBufferedWriter(
                reportPath,
                StandardCharsets.UTF_8
        )) {
            writer.write("=== BILAN DES ERREURS ===\n\n");

            if (failedSteps.isEmpty()) {
                writer.write("Aucune erreur détectée.\n");
                return;
            }

            for (StepExecution step : failedSteps) {
                writer.write("Step : " + step.getStepName() + "\n");
                writer.write("Status : " + step.getStatus() + "\n");
                writer.write(
                        "ExitStatus : " + step.getExitStatus() + "\n\n"
                );

                for (Throwable failure : step.getFailureExceptions()) {
                    writer.write("Exception :\n");
                    writer.write(
                            failure.getClass().getName()
                                    + " : "
                                    + failure.getMessage()
                                    + "\n\n"
                    );
                }

                writer.write(
                        "----------------------------------------\n\n"
                );
            }

        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Impossible d'écrire le rapport des erreurs : " + reportPath,
                    e
            );
        }
    }
    private void writeRejectedReport(JobExecution jobExecution, String timestamp) {
        String jobName = jobExecution.getJobInstance().getJobName();

        Path reportPath = Path.of(
                bilanDir,
                "rapport_rejetes_" + jobName + "_" + timestamp + ".txt"
        );

        Collection<?> rejectedIds = "importDvfJob".equals(jobName)
                ? dvfSkipListener.getIdsRejetes()
                : skipListener.getIdsRejetes();

        try (BufferedWriter writer = Files.newBufferedWriter(
                reportPath,
                StandardCharsets.UTF_8
        )) {
            writer.write("=== BILAN DES LIGNES REJETÉES ===\n\n");
            writer.write(
                    "Nombre de lignes rejetées : "
                            + rejectedIds.size()
                            + "\n\n"
            );

            for (Object id : rejectedIds) {
                writer.write(String.valueOf(id));
                writer.newLine();
            }

        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Impossible d'écrire le rapport des rejets : " + reportPath,
                    e
            );
        }
    }
}




