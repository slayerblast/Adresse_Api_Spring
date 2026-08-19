package fr.natsystem.projet.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class checkFileListener implements JobExecutionListener {
    @Value("${spring.batch.bilanDir}")
    private String bilanDir;
    private String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

    @Override
    public void afterJob(JobExecution je) {

        String status = je.getExecutionContext().getString("lastDeciderStatus", "");
        if (je.getStatus() == BatchStatus.FAILED) {
            try (FileWriter writer = new FileWriter(bilanDir + "rapport_" + je.getJobInstance().getJobName() + "_" + timestamp + ".txt")) {
                Duration jobDuration = Duration.between(je.getStartTime(), je.getEndTime());

                writer.write("=== STATUS DU JOB ===\n\n");
                writer.write("Status : " + je.getStatus() + "\n");
                writer.write("ExitStatus : " + status + "\n\n");
                writer.write("Début : " + je.getStartTime() + "\n");
                writer.write("Fin    : " + je.getEndTime() + "\n");
                writer.write("Durée totale : " + jobDuration.toSeconds() + " secondes\n\n");
                writer.write("Aucun fichier à traiter");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
