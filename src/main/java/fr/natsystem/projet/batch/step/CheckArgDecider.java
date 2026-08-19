package fr.natsystem.projet.batch.step;

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

@Slf4j
@Component
public class CheckArgDecider implements JobExecutionDecider {
    @Value("${spring.batch.retriever}")
    private boolean retriever;
    private FlowExecutionStatus result ;
    @Value("${spring.batch.pathFile}")
    private String pathFile;

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, @Nullable StepExecution stepExecution) {
        boolean fileExist;
        String inputFile = jobExecution.getJobParameters().getString("inputFile");
        File file = new File(inputFile);

        fileExist = file.exists() && file.isFile();
        File folder = new File(pathFile);
        long count = Arrays.stream(folder.listFiles())
                .filter(File::isFile)
                .count();

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
