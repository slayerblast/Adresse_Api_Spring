package fr.natsystem.projet.Decider;

import fr.natsystem.projet.batch.Decider.CheckArgDecider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CheckArgDeciderTest {

    @TempDir
    Path tempDirectory;

    @Test
    void shouldReturnOkFileExistWhenInputFileExists() throws IOException {
        Path inputFile = Files.writeString(
                tempDirectory.resolve("adresses.csv"),
                "contenu du fichier"
        );

        CheckArgDecider decider = createDecider(
                false,
                tempDirectory
        );

        JobExecution jobExecution = createJobExecution(
                inputFile.toString()
        );

        FlowExecutionStatus result = decider.decide(
                jobExecution,
                null
        );

        assertEquals("OK_FILE_EXIST", result.getName());

        assertEquals(
                "OK_FILE_EXIST",
                jobExecution.getExecutionContext()
                        .getString("lastDeciderStatus")
        );

        assertFalse(
                jobExecution.getExecutionContext()
                        .getString("checksum", "")
                        .isBlank()
        );
    }

    @Test
    void shouldReturnMultipleFilesFoundWhenFolderContainsSeveralFiles()
            throws IOException {

        Files.createFile(
                tempDirectory.resolve("file1.csv")
        );

        Files.createFile(
                tempDirectory.resolve("file2.csv")
        );

        CheckArgDecider decider = createDecider(
                false,
                tempDirectory
        );

        JobExecution jobExecution = createJobExecution(
                tempDirectory.resolve("absent.csv").toString()
        );

        FlowExecutionStatus result = decider.decide(
                jobExecution,
                null
        );

        assertEquals(
                "MULTIPLE_FILES_FOUND",
                result.getName()
        );

        assertEquals(
                "MULTIPLE_FILES_FOUND",
                jobExecution.getExecutionContext()
                        .getString("lastDeciderStatus")
        );
    }

    @Test
    void shouldReturnNoInputFileWhenRetrieverIsDisabledAndFolderIsEmpty() {
        CheckArgDecider decider = createDecider(
                false,
                tempDirectory
        );

        JobExecution jobExecution = createJobExecution(
                tempDirectory.resolve("absent.csv").toString()
        );

        FlowExecutionStatus result = decider.decide(
                jobExecution,
                null
        );

        assertEquals("NO_INPUT_FILE", result.getName());

        assertEquals(
                "NO_INPUT_FILE",
                jobExecution.getExecutionContext()
                        .getString("lastDeciderStatus")
        );
    }

    @Test
    void shouldReturnNoInputFileWhenRetrieverIsDisabledAndFolderContainsOneFile()
            throws IOException {

        Files.createFile(
                tempDirectory.resolve("another-file.csv")
        );

        CheckArgDecider decider = createDecider(
                false,
                tempDirectory
        );

        JobExecution jobExecution = createJobExecution(
                tempDirectory.resolve("absent.csv").toString()
        );

        FlowExecutionStatus result = decider.decide(
                jobExecution,
                null
        );

        assertEquals("NO_INPUT_FILE", result.getName());

        assertEquals(
                "NO_INPUT_FILE",
                jobExecution.getExecutionContext()
                        .getString("lastDeciderStatus")
        );
    }

    @Test
    void shouldReturnMultipleFilesFoundWhenRetrieverIsEnabledAndFolderContainsOneFile()
            throws IOException {

        Files.createFile(
                tempDirectory.resolve("another-file.csv")
        );

        CheckArgDecider decider = createDecider(
                true,
                tempDirectory
        );

        JobExecution jobExecution = createJobExecution(
                tempDirectory.resolve("absent.csv").toString()
        );

        FlowExecutionStatus result = decider.decide(
                jobExecution,
                null
        );

        assertEquals(
                "MULTIPLE_FILES_FOUND",
                result.getName()
        );

        assertEquals(
                "MULTIPLE_FILES_FOUND",
                jobExecution.getExecutionContext()
                        .getString("lastDeciderStatus")
        );
    }

    @Test
    void shouldReturnOkForRetrieveWhenRetrieverIsEnabledAndFolderIsEmpty() {
        CheckArgDecider decider = createDecider(
                true,
                tempDirectory
        );

        JobExecution jobExecution = createJobExecution(
                tempDirectory.resolve("absent.csv").toString()
        );

        FlowExecutionStatus result = decider.decide(
                jobExecution,
                null
        );

        assertEquals("OK_FOR_RETRIEVE", result.getName());

        assertEquals(
                "OK_FOR_RETRIEVE",
                jobExecution.getExecutionContext()
                        .getString("lastDeciderStatus")
        );
    }

    @Test
    void shouldReturnNoInputFileWhenInputFileParameterIsMissingAndRetrieverIsDisabled() {
        CheckArgDecider decider = createDecider(
                false,
                tempDirectory
        );

        JobExecution jobExecution =
                createJobExecutionWithoutInputFile();

        FlowExecutionStatus result = decider.decide(
                jobExecution,
                null
        );

        assertEquals("NO_INPUT_FILE", result.getName());

        assertEquals(
                "NO_INPUT_FILE",
                jobExecution.getExecutionContext()
                        .getString("lastDeciderStatus")
        );
    }

    @Test
    void shouldReturnOkForRetrieveWhenInputFileParameterIsMissingAndRetrieverIsEnabled() {
        CheckArgDecider decider = createDecider(
                true,
                tempDirectory
        );

        JobExecution jobExecution =
                createJobExecutionWithoutInputFile();

        FlowExecutionStatus result = decider.decide(
                jobExecution,
                null
        );

        assertEquals("OK_FOR_RETRIEVE", result.getName());

        assertEquals(
                "OK_FOR_RETRIEVE",
                jobExecution.getExecutionContext()
                        .getString("lastDeciderStatus")
        );
    }

    private CheckArgDecider createDecider(
            boolean retriever,
            Path folder
    ) {
        CheckArgDecider decider = new CheckArgDecider();

        ReflectionTestUtils.setField(
                decider,
                "retriever",
                retriever
        );

        ReflectionTestUtils.setField(
                decider,
                "pathFile",
                folder.toString()
        );

        return decider;
    }

    private JobExecution createJobExecution(String inputFile) {
        JobParameters parameters = new JobParametersBuilder()
                .addString("inputFile", inputFile)
                .addLong("startAt", System.currentTimeMillis())
                .addString("innerJob", "importAdresseJob")
                .toJobParameters();

        JobInstance jobInstance = new JobInstance(
                1L,
                "checkArgTestJob"
        );

        return new JobExecution(
                1L,
                jobInstance,
                        parameters
                );
    }

    private JobExecution createJobExecutionWithoutInputFile() {
        JobParameters parameters = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis())
                .addString("innerJob", "importAdresseJob")
                .toJobParameters();

        JobInstance jobInstance = new JobInstance(
                1L,
                "checkArgTestJob"
        );

        return new JobExecution(
                1L,
                jobInstance,
                        parameters
                );
    }
}