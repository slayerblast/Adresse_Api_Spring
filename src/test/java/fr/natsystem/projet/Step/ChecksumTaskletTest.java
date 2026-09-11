package fr.natsystem.projet.Step;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.natsystem.projet.batch.step.ChecksumTasklet;
import fr.natsystem.projet.services.ChecksumUtils;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.test.util.ReflectionTestUtils;

class ChecksumTaskletTest {

  @TempDir Path temporaryDirectory;

  private Path inputDirectory;
  private Path archiveDirectory;
  private static final int TAILLE_CHEKSUM = 64;
  private ChecksumTasklet tasklet;

  private StepContribution contribution;
  private ChunkContext chunkContext;
  private JobParameters jobParameters;
  private ExecutionContext executionContext;

  @BeforeEach
  void setUp() throws Exception {
    inputDirectory = Files.createDirectory(temporaryDirectory.resolve("input"));

    archiveDirectory = Files.createDirectory(temporaryDirectory.resolve("archive"));

    tasklet = new ChecksumTasklet();

    ReflectionTestUtils.setField(tasklet, "pathFile", inputDirectory.toString());

    ReflectionTestUtils.setField(tasklet, "archiveDir", archiveDirectory.toString());

    contribution = mock(StepContribution.class);
    chunkContext = mock(ChunkContext.class);
    StepExecution stepExecution = mock(StepExecution.class);
    JobExecution jobExecution = mock(JobExecution.class);
    jobParameters = mock(JobParameters.class);

    executionContext = new ExecutionContext();

    when(contribution.getStepExecution()).thenReturn(stepExecution);

    when(stepExecution.getJobParameters()).thenReturn(jobParameters);

    when(stepExecution.getJobExecution()).thenReturn(jobExecution);

    when(jobExecution.getExecutionContext()).thenReturn(executionContext);
  }

  @Test
  void shouldMoveAdresseFileAndStoreChecksum() throws Exception {
    Path sourceFile = createInputFile("adresse-source.csv", "id;nom\n1;Dupont");

    String expectedChecksum = ChecksumUtils.sha256(sourceFile.toAbsolutePath().toString());

    when(jobParameters.getString("innerJob")).thenReturn("importAdresseJob");

    RepeatStatus result = tasklet.execute(contribution, chunkContext);

    String timestamp = getTaskletTimestamp();

    Path expectedDestination = archiveDirectory.resolve(timestamp + "_adresse.csv");

    assertAll(
        () -> assertEquals(RepeatStatus.FINISHED, result),
        () -> assertFalse(Files.exists(sourceFile), "Le fichier source ne doit plus exister"),
        () ->
            assertTrue(
                Files.exists(expectedDestination), "Le fichier doit être présent dans l'archive"),
        () -> assertEquals(expectedChecksum, executionContext.getString("checksum")));

    String archivedContent = Files.readString(expectedDestination, StandardCharsets.UTF_8);

    assertEquals("id;nom\n1;Dupont", archivedContent);
  }

  @Test
  void shouldMoveDvfFileAndStoreChecksum() throws Exception {
    Path sourceFile = createInputFile("dvf-source.csv", "id;prix\n1;250000");

    String expectedChecksum = ChecksumUtils.sha256(sourceFile.toAbsolutePath().toString());

    when(jobParameters.getString("innerJob")).thenReturn("importDvfJob");

    RepeatStatus result = tasklet.execute(contribution, chunkContext);

    String timestamp = getTaskletTimestamp();

    Path expectedDestination = archiveDirectory.resolve(timestamp + "_dvf.csv");

    assertAll(
        () -> assertEquals(RepeatStatus.FINISHED, result),
        () -> assertFalse(Files.exists(sourceFile)),
        () -> assertTrue(Files.exists(expectedDestination)),
        () -> assertEquals(expectedChecksum, executionContext.getString("checksum")));
  }

  @Test
  void shouldUseAdresseDestinationForUnknownJob() throws Exception {
    Path sourceFile = createInputFile("unknown-source.csv", "contenu du fichier");

    when(jobParameters.getString("innerJob")).thenReturn("importAutreJob");

    RepeatStatus result = tasklet.execute(contribution, chunkContext);

    String timestamp = getTaskletTimestamp();

    Path expectedDestination = archiveDirectory.resolve(timestamp + "_adresse.csv");

    assertAll(
        () -> assertEquals(RepeatStatus.FINISHED, result),
        () -> assertFalse(Files.exists(sourceFile)),
        () -> assertTrue(Files.exists(expectedDestination)),
        () -> assertTrue(executionContext.containsKey("checksum")));
  }

  @Test
  void shouldReplaceExistingArchivedFile() throws Exception {
    Path sourceFile = createInputFile("new-source.csv", "nouveau contenu");

    when(jobParameters.getString("innerJob")).thenReturn("importAdresseJob");

    String timestamp = getTaskletTimestamp();

    Path existingDestination = archiveDirectory.resolve(timestamp + "_adresse.csv");

    Files.writeString(existingDestination, "ancien contenu", StandardCharsets.UTF_8);

    tasklet.execute(contribution, chunkContext);

    assertAll(
        () -> assertFalse(Files.exists(sourceFile)),
        () -> assertTrue(Files.exists(existingDestination)),
        () ->
            assertEquals(
                "nouveau contenu", Files.readString(existingDestination, StandardCharsets.UTF_8)));
  }

  @Test
  void shouldCreateSha256ChecksumWithExpectedLength() throws Exception {

    createInputFile("input.csv", "contenu à vérifier");

    when(jobParameters.getString("innerJob")).thenReturn("importAdresseJob");

    tasklet.execute(contribution, chunkContext);

    String checksum = executionContext.getString("checksum");

    assertAll(
        () -> assertNotNull(checksum),
        () -> assertFalse(checksum.isBlank()),
        () -> assertEquals(TAILLE_CHEKSUM, checksum.length()));
  }

  @Test
  void shouldFailWhenInputDirectoryIsEmpty() {
    when(jobParameters.getString("innerJob")).thenReturn("importAdresseJob");

    assertThrows(
        ArrayIndexOutOfBoundsException.class, () -> tasklet.execute(contribution, chunkContext));

    assertFalse(executionContext.containsKey("checksum"));
  }

  @Test
  void shouldFailWhenInnerJobIsNull() throws Exception {
    createInputFile("input.csv", "contenu");

    when(jobParameters.getString("innerJob")).thenReturn(null);

    /*
     * Dans l'implémentation actuelle, destination reste à null.
     * Files.move échoue donc au moment du déplacement.
     */
    assertThrows(NullPointerException.class, () -> tasklet.execute(contribution, chunkContext));

    /*
     * Le checksum est enregistré avant la tentative de déplacement.
     */
    assertTrue(executionContext.containsKey("checksum"));
  }

  @Test
  void shouldFailWhenInputDirectoryDoesNotExist() {
    Path missingDirectory = temporaryDirectory.resolve("dossier-inexistant");

    ReflectionTestUtils.setField(tasklet, "pathFile", missingDirectory.toString());

    when(jobParameters.getString("innerJob")).thenReturn("importAdresseJob");

    assertThrows(NullPointerException.class, () -> tasklet.execute(contribution, chunkContext));
  }

  private Path createInputFile(String fileName, String content) throws Exception {

    return Files.writeString(inputDirectory.resolve(fileName), content, StandardCharsets.UTF_8);
  }

  private String getTaskletTimestamp() {
    String timestamp = (String) ReflectionTestUtils.getField(tasklet, "timestamp");

    assertNotNull(timestamp);

    return timestamp;
  }
}
