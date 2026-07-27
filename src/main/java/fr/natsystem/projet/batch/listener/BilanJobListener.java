package fr.natsystem.projet.batch.listener;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
@Getter
@Setter
public class BilanJobListener implements JobExecutionListener {
    private int doublon = 0;
    private int doublonPur = 0;
    private  int obsolete = 0 ;
    private final AdresseSkipListener skipListener;

    @Override
    public void beforeJob(JobExecution je) {
        log.info("Job [{}] demarre",
                je.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution je) {

        log.info("Job {} : {}", je.getJobInstance()
                .getJobName(), je.getStatus());

        // ============================
        // BILAN IMPORT CSV -> STAGING
        // ============================

        StepExecution importSet = je.getStepExecutions().stream()
                .filter(s-> s.getStepName().equals("importCsvStep"))
                .findFirst().orElse(null);
        if (importSet != null) {

            try (FileWriter writer = new FileWriter( "src/main/resources/bilan/bilan_import_csv.txt")) {
                writer.write("=== BILAN IMPORT CSV -> STAGING ===\n\n");
                writer.write("ReadCount  : " + importSet.getReadCount() + "\n" );
                writer.write("WriteCount : "+ importSet.getWriteCount() + "\n" );
                writer.write("Lignes qui n'ont pas passé le BeanValidation : "+ importSet.getFilterCount() + "\n");
                writer.write("Nombre d'ID rejetés : "+ skipListener.getIdsRejetes().size() + "\n");
                /*
                writer.write("\nIds rejetés :\n");
                for (String id : skipListener.getRejetesIds()) {
                    writer.write(id + "\n");
                }
                */
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // ============================
        // BILAN IMPORT ADRESSE
        // ============================

        importSet = je.getStepExecutions().stream()
                .filter(s-> s.getStepName().equals("importAdresseStep"))
                .findFirst().orElse(null);
        if(importSet!=null){
            try (FileWriter writer = new FileWriter("src/main/resources/bilan/bilan.txt")) {
                writer.write("=== BILAN IMPORT ===\n");
                writer.write("ReadCount  : " + importSet.getReadCount() + "\n");
                writer.write("WriteCount : " + importSet.getWriteCount() + "\n");
                writer.write("Doublons pur : " + doublonPur + "\n");
                writer.write("Lignes en double : " + doublon + "\n");
                writer.write("Lignes obsolète supprimées: " + obsolete + "\n");

                /*
                writer.write("\nIds rejetés :\n");

                for (String id : skipListener.getRejetesIds()) {
                    writer.write(id + "\n");
                }

                writer.write("\nIds skippés :\n");
                for (String id : skipListener.getSkippedIds()) {
                    writer.write(id + "\n");
                }

                 */

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // ============================
        // BILAN du temps par etape
        // ============================
        try (FileWriter writer =
                     new FileWriter("src/main/resources/bilan/bilan_temps.txt")) {

            writer.write("=== BILAN DES TEMPS ===\n\n");

            Duration jobDuration = Duration.between(
                    je.getStartTime(),
                    je.getEndTime()
            );

            writer.write("Début : " + je.getStartTime() + "\n");
            writer.write("Fin    : " + je.getEndTime() + "\n");
            writer.write("Durée totale : " + jobDuration.toSeconds() + " secondes\n\n");

            writer.write("=== Temps par étape ===\n");

            for (StepExecution step : je.getStepExecutions()) {

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

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}



