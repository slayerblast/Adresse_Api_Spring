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

    @Value("${spring.batch.urlPathFile}")
    private String url;

    private final FileDownloadService fileDownloadService;

    @Override
    public RepeatStatus execute(
            StepContribution contribution,
            ChunkContext chunkContext) throws Exception {

        String csvPath = fileDownloadService.downloadAndUngzip(url);
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
        return RepeatStatus.FINISHED;
    }
}