package fr.natsystem.projet.Decider;

import fr.natsystem.projet.batch.Decider.FileCountDecider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;

import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileCountDeciderTest {

    @TempDir
    Path temporaryDirectory;

    private FileCountDecider decider;

    @BeforeEach
    void setUp() {
        decider = new FileCountDecider();

        ReflectionTestUtils.setField(
                decider,
                "pathFile",
                temporaryDirectory.toString()
        );
    }

    @Test
    void shouldReturnOkArgNotEmptyWhenInputFileIsProvided() {
        setRetriever(false);

        JobExecution jobExecution = createJobExecution("adresses.csv");

        FlowExecutionStatus result =
                decider.decide(jobExecution, null);

        assertEquals("OK_ARG_NOT_EMPTY", result.getName());

        assertEquals(
                "OK_ARG_NOT_EMPTY",
                jobExecution.getExecutionContext()
                        .getString("lastDeciderStatus")
        );
    }

    @Test
    void shouldGivePriorityToInputFileEvenWhenSeveralFilesExist()
            throws Exception {

        setRetriever(false);

        createFile("file1.csv", "contenu 1");
        createFile("file2.csv", "contenu 2");

        JobExecution jobExecution =
                createJobExecution("fichier-demande.csv");

        FlowExecutionStatus result =
                decider.decide(jobExecution, null);

        assertEquals("OK_ARG_NOT_EMPTY", result.getName());
    }

    @Test
    void shouldReturnMultipleFilesFoundWhenSeveralFilesExist()
            throws Exception {

        setRetriever(false);

        createFile("file1.csv", "contenu 1");
        createFile("file2.csv", "contenu 2");

        JobExecution jobExecution = createJobExecution("");

        FlowExecutionStatus result =
                decider.decide(jobExecution, null);

        assertEquals("MULTIPLE_FILES_FOUND", result.getName());

        assertEquals(
                "MULTIPLE_FILES_FOUND",
                jobExecution.getExecutionContext()
                        .getString("lastDeciderStatus")
        );
    }

    @Test
    void shouldReturnMultipleFilesFoundRegardlessOfRetrieverWhenSeveralFilesExist()
            throws Exception {

        setRetriever(true);

        createFile("file1.csv", "contenu 1");
        createFile("file2.csv", "contenu 2");

        JobExecution jobExecution = createJobExecution("");

        FlowExecutionStatus result =
                decider.decide(jobExecution, null);

        assertEquals("MULTIPLE_FILES_FOUND", result.getName());
    }

    @Test
    void shouldReturnNoInputFileWhenDirectoryIsEmptyAndRetrieverIsDisabled() {
        setRetriever(false);

        JobExecution jobExecution = createJobExecution("");

        FlowExecutionStatus result =
                decider.decide(jobExecution, null);

        assertEquals("NO_INPUT_FILE", result.getName());

        assertEquals(
                "NO_INPUT_FILE",
                jobExecution.getExecutionContext()
                        .getString("lastDeciderStatus")
        );
    }

    @Test
    void shouldReturnOkForRetrieveWhenDirectoryIsEmptyAndRetrieverIsEnabled() {
        setRetriever(true);

        JobExecution jobExecution = createJobExecution("");

        FlowExecutionStatus result =
                decider.decide(jobExecution, null);

        assertEquals("OK_FOR_RETRIEVE", result.getName());

        assertEquals(
                "OK_FOR_RETRIEVE",
                jobExecution.getExecutionContext()
                        .getString("lastDeciderStatus")
        );
    }

    @Test
    void shouldReturnOkForImportAndStoreChecksumWhenOneFileExists()
            throws Exception {

        setRetriever(false);

        createFile(
                "input.csv",
                "id;nom\n1;Dupont"
        );

        JobExecution jobExecution = createJobExecution("");

        FlowExecutionStatus result =
                decider.decide(jobExecution, null);

        assertEquals("OK_FOR_IMPORT", result.getName());

        ExecutionContext executionContext =
                jobExecution.getExecutionContext();

        assertTrue(executionContext.containsKey("checksum"));

        String checksum =
                executionContext.getString("checksum");

        assertNotNull(checksum);
        assertFalse(checksum.isBlank());

        // Un checksum SHA-256 en représentation hexadécimale
        // contient normalement 64 caractères.
        assertEquals(64, checksum.length());

        assertEquals(
                "OK_FOR_IMPORT",
                executionContext.getString("lastDeciderStatus")
        );
    }

    @Test
    void shouldReturnMultipleFilesFoundWhenOneFileExistsAndRetrieverIsEnabled()
            throws Exception {

        setRetriever(true);

        createFile("input.csv", "contenu du fichier");

        JobExecution jobExecution = createJobExecution("");

        FlowExecutionStatus result =
                decider.decide(jobExecution, null);

        assertEquals("MULTIPLE_FILES_FOUND", result.getName());

        assertFalse(
                jobExecution.getExecutionContext()
                        .containsKey("checksum")
        );

        assertEquals(
                "MULTIPLE_FILES_FOUND",
                jobExecution.getExecutionContext()
                        .getString("lastDeciderStatus")
        );
    }

    @Test
    void shouldTreatBlankInputFileAsEmpty() {
        setRetriever(false);

        JobExecution jobExecution =
                createJobExecution("   ");

        FlowExecutionStatus result =
                decider.decide(jobExecution, null);

        assertEquals("NO_INPUT_FILE", result.getName());
    }

    @Test
    void shouldIgnoreSubdirectoriesWhenCountingFiles()
            throws Exception {

        setRetriever(false);

        Files.createDirectory(
                temporaryDirectory.resolve("sous-dossier")
        );

        JobExecution jobExecution = createJobExecution("");

        FlowExecutionStatus result =
                decider.decide(jobExecution, null);

        assertEquals("NO_INPUT_FILE", result.getName());
    }

    @Test
    void shouldCountOnlyFilesAndIgnoreSubdirectories()
            throws Exception {

        setRetriever(false);

        createFile("input.csv", "contenu");

        Files.createDirectory(
                temporaryDirectory.resolve("sous-dossier")
        );

        JobExecution jobExecution = createJobExecution("");

        FlowExecutionStatus result =
                decider.decide(jobExecution, null);

        assertEquals("OK_FOR_IMPORT", result.getName());
    }

    private void setRetriever(boolean retriever) {
        ReflectionTestUtils.setField(
                decider,
                "retriever",
                retriever
        );
    }

    private void createFile(
            String fileName,
            String content
    ) throws Exception {

        Files.writeString(
                temporaryDirectory.resolve(fileName),
                content
        );
    }

    private JobExecution createJobExecution(String inputFile) {
        JobExecution jobExecution =
                mock(JobExecution.class);

        JobParameters jobParameters =
                mock(JobParameters.class);

        ExecutionContext executionContext =
                new ExecutionContext();

        when(jobExecution.getJobParameters())
                .thenReturn(jobParameters);

        when(jobExecution.getExecutionContext())
                .thenReturn(executionContext);

        when(jobParameters.getString("inputFile", ""))
                .thenReturn(inputFile);

        return jobExecution;
    }
}