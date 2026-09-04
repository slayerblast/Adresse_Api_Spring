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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChecksumTasklet implements Tasklet {
    @Value("${spring.batch.pathFile}")
    private String pathFile;
    private final String timestamp = ZonedDateTime.now(ZoneId.of("Europe/Paris")).format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    @Value("${spring.batch.archive}")
    private String archiveDir;

    @Override
    public @Nullable RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        String checkSum;
        String innerJobParent = contribution.getStepExecution().getJobParameters().getString("innerJob");
        File folder = new File(pathFile);
        Path destination = null;
        File[] files = folder.listFiles(File::isFile);
        checkSum = ChecksumUtils.sha256(files[0].getAbsolutePath());
        contribution.getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .putString("checksum", checkSum);
        Path source = Paths.get(files[0].getAbsolutePath());
        if(innerJobParent != null){
            if (innerJobParent.equals("importAdresseJob"))
            {
                destination = Paths.get(archiveDir).resolve(timestamp+"_adresse.csv");
            }
            else if (innerJobParent.equals("importDvfJob"))
            {
                destination = Paths.get(archiveDir).resolve(timestamp+"_dvf.csv");
            } else {
                destination = Paths.get(archiveDir).resolve(timestamp+"_adresse.csv");
            }
        }


        Files.move(
                source,
                destination,
                StandardCopyOption.REPLACE_EXISTING
        );
        return RepeatStatus.FINISHED;
    }
}
