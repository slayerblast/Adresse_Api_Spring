package fr.natsystem.projet.batch.step;

import fr.natsystem.projet.services.ChecksumUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChecksumTasklet implements Tasklet {
    @Value("${spring.batch.pathFile}")
    private String pathFile;
    private String checkSum;
    private String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    @Value("${spring.batch.archive}")
    private String archiveDir;

    @Override
    public @Nullable RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        File folder = new File(pathFile);
        File[] files = folder.listFiles(File::isFile);
        checkSum = ChecksumUtils.sha256(files[0].getAbsolutePath());
        contribution.getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .putString("checksum", checkSum);
        Path source = Paths.get(files[0].getAbsolutePath());
        Path destination = Paths.get(archiveDir).resolve(timestamp+"_adresse.csv");
        Files.move(
                source,
                destination,
                StandardCopyOption.REPLACE_EXISTING
        );
        return RepeatStatus.FINISHED;
    }
}
