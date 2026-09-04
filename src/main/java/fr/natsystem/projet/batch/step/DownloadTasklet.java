package fr.natsystem.projet.batch.step;

import fr.natsystem.projet.services.ChecksumUtils;
import fr.natsystem.projet.services.FileDownloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DownloadTasklet implements Tasklet {

    @Value("${spring.batch.urlPathFileAdresse}")
    private String urlAdresse;

    @Value("${spring.batch.urlPathFileDvf}")
    private String urlDvf;

    private final FileDownloadService fileDownloadService;

    @Override
    public RepeatStatus execute(
            StepContribution contribution,
            ChunkContext chunkContext) throws Exception {
        String innerJob = contribution.getStepExecution().getJobExecution().getJobParameters().getString("innerJob");
        String csvPath ="";
        if(innerJob!=null){
            if (innerJob.equals("importAdresseJob")) {
                csvPath = fileDownloadService.downloadAndUngzip(urlAdresse);
            } else if (innerJob.equals("importDvfJob")) {
                csvPath = fileDownloadService.downloadAndUngzip(urlDvf);
            }else {
                csvPath = fileDownloadService.downloadAndUngzip(urlAdresse);
            }

            if (csvPath.isBlank()) {
                chunkContext.getStepContext()
                        .getStepExecution()
                        .getJobExecution()
                        .getExecutionContext()
                        .putString("noFile", "Not found");
                chunkContext.getStepContext()
                        .getStepExecution()
                        .getJobExecution()
                        .getExecutionContext()
                        .putString("lastDeciderStatus", "NO_INPUT_FILE");
                contribution.setExitStatus(new ExitStatus("NO_INPUT_FILE"));

                return RepeatStatus.FINISHED;
            }

            String checksum = ChecksumUtils.sha256(csvPath);

            chunkContext.getStepContext()
                    .getStepExecution()
                    .getJobExecution()
                    .getExecutionContext()
                    .putString("inputFile", csvPath);

            chunkContext.getStepContext()
                    .getStepExecution()
                    .getJobExecution()
                    .getExecutionContext()
                    .putString("checksum", checksum);
            chunkContext.getStepContext()
                    .getStepExecution()
                    .getJobExecution()
                    .getExecutionContext()
                    .putString("lastDeciderStatus", "READY");
            contribution.setExitStatus(new ExitStatus("READY"));
        }

        return RepeatStatus.FINISHED;
    }
}