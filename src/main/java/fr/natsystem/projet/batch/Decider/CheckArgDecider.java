package fr.natsystem.projet.batch.Decider;

import fr.natsystem.projet.services.ChecksumUtils;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

@Slf4j
@Component
public class CheckArgDecider implements JobExecutionDecider {
    @Value("${spring.batch.retriever}")
    private boolean retriever;
    @Value("${spring.batch.pathFile}")
    private String pathFile;

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, @Nullable StepExecution stepExecution) {
        FlowExecutionStatus result;
        boolean fileExist = false;
        long count = 0;
        File folder = null;
        String inputFile = jobExecution.getJobParameters().getString("inputFile");

        File file = inputFile == null ? null : new File(inputFile);
        if (file != null) {
            fileExist = file.exists() && file.isFile();
            folder = new File(pathFile);
             count = Arrays.stream(Objects.requireNonNull(folder.listFiles()))
                    .filter(File::isFile)
                    .count();
        }

        if (fileExist) {
            result = new FlowExecutionStatus("OK_FILE_EXIST"); // le fichier existe et correspond à la valeur de l'argument
            File[] files = folder.listFiles(File::isFile);
            String checkSum = null;
            try {
                checkSum = ChecksumUtils.sha256(files[0].getAbsolutePath());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            jobExecution.getExecutionContext().putString("checksum", checkSum);
        } else if (count > 1) {
            result = new FlowExecutionStatus("MULTIPLE_FILES_FOUND"); // il y a au moins deux fichiers dans le dossier et aucune ne correspond à l'argument
        } else if(!retriever) {
            result = new FlowExecutionStatus("NO_INPUT_FILE"); //  il y a 1 fichier ou 0 et le paramètre de récuperation est désactivé
        }else if(count == 1) {
            result = new FlowExecutionStatus("MULTIPLE_FILES_FOUND"); // il y a un fichier et le paramètre de récupération est activé donc preshot de l'erreur MULTIPLE_FILES_FOUND
        }else {
            result = new FlowExecutionStatus("OK_FOR_RETRIEVE"); // il y a 0 fichier et le paramètre de récupération est activé
        }
        jobExecution.getExecutionContext().putString("lastDeciderStatus", result.getName());
        return result;
    }
}
