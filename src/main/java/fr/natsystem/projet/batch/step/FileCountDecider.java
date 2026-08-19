package fr.natsystem.projet.batch.step;


import fr.natsystem.projet.services.ChecksumUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;

@Component
public class FileCountDecider implements JobExecutionDecider {
    @Value("${spring.batch.retriever}")
    private boolean retriever;
    private FlowExecutionStatus result;
    @Value("${spring.batch.pathFile}")
    private String pathFile;

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, @Nullable StepExecution stepExecution) {
        String inputFile = jobExecution.getJobParameters().getString("inputFile");
        File folder = new File(pathFile);
        long count = Arrays.stream(folder.listFiles())
                .filter(File::isFile)
                .count();

        if(inputFile.length() > 1)
        {
            result =  new FlowExecutionStatus("OK_ARG_NOT_EMPTY"); // l'argument n'est pas vide l'argument
        }else if(count > 1 ) {
            result =  new FlowExecutionStatus("MULTIPLE_FILES_FOUND"); // plus de 1 fichier + aucune correspondance avec l'argument
        } else if (count < 1 && !retriever) {
            result =  new FlowExecutionStatus("NO_INPUT_FILE"); // le fichier correspondant à l'argument n'est pas trouvé + param de récuperation désactivé
        }else if (count == 1 && !retriever) {
            result =  new FlowExecutionStatus("OK_FOR_IMPORT"); // un fichier trouvé + param de récuperation désactivé donc on lit le fichier
            File[] files = folder.listFiles(File::isFile);
            String checkSum = null;
            try {
                checkSum = ChecksumUtils.sha256(files[0].getAbsolutePath());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            jobExecution.getExecutionContext().putString("checksum", checkSum);
        }else if (count == 1 && retriever) {
            result =  new FlowExecutionStatus("MULTIPLE_FILES_FOUND"); // un fichier trouvé + param de récuperation activé mais preshot de l'erreur MULTIPLE_FILES_FOUND
        }else if (count == 0 && retriever) {
            result =  new FlowExecutionStatus("OK_FOR_RETRIEVE"); //Aucun fichier trouvé + param de récuperation activé donc OK pour la suite
        }
        jobExecution.getExecutionContext().putString("lastDeciderStatus", result.getName());
        return result;
    }
}
