package fr.natsystem.projet.Step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.natsystem.projet.batch.step.DownloadTasklet;
import fr.natsystem.projet.services.ChecksumUtils;
import fr.natsystem.projet.services.FileDownloadService;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DownloadTaskletTest {

  private static final String URL_ADRESSE = "https://example.test/adresse.csv.gz";
  private static final long ID = 3L;
  private static final String URL_DVF = "https://example.test/dvf.csv.gz";

  @Mock private FileDownloadService fileDownloadService;

  private DownloadTasklet downloadTasklet;

  @TempDir Path temporaryDirectory;

  @BeforeEach
  void setUp() {
    downloadTasklet = new DownloadTasklet(fileDownloadService);

    ReflectionTestUtils.setField(downloadTasklet, "urlAdresse", URL_ADRESSE);

    ReflectionTestUtils.setField(downloadTasklet, "urlDvf", URL_DVF);
  }

  @ParameterizedTest
  @MethodSource("downloadJobs")
  void shouldDownloadFileAndPrepareImport(String jobName, String fileName, String url)
      throws Exception {

    Path csvFile = createCsvFile(fileName);

    when(fileDownloadService.downloadAndUngzip(url)).thenReturn(csvFile.toString());

    TestContext context = createTestContext(jobName);

    RepeatStatus result = downloadTasklet.execute(context.contribution(), context.chunkContext());

    String expectedChecksum = ChecksumUtils.sha256(csvFile.toString());

    assertEquals(RepeatStatus.FINISHED, result);
    assertEquals(new ExitStatus("READY"), context.contribution().getExitStatus());

    assertEquals(csvFile.toString(), context.executionContext().getString("inputFile"));

    assertEquals(expectedChecksum, context.executionContext().getString("checksum"));
  }

  @Test
  void shouldUseAdresseUrlForUnknownJob() throws Exception {

    Path csvFile = createCsvFile("unknown.csv");

    when(fileDownloadService.downloadAndUngzip(URL_ADRESSE)).thenReturn(csvFile.toString());

    TestContext context = createTestContext("unknownJob");

    RepeatStatus result = downloadTasklet.execute(context.contribution(), context.chunkContext());

    assertEquals(RepeatStatus.FINISHED, result);

    assertEquals(new ExitStatus("READY"), context.contribution().getExitStatus());

    assertEquals(csvFile.toString(), context.executionContext().getString("inputFile"));

    assertEquals("READY", context.executionContext().getString("lastDeciderStatus"));

    verify(fileDownloadService).downloadAndUngzip(URL_ADRESSE);

    verify(fileDownloadService, never()).downloadAndUngzip(URL_DVF);
  }

  @ParameterizedTest
  @MethodSource("emptyDownloadCases")
  void shouldSetNoInputFileWhenDownloadReturnsEmptyOrBlankPath(
      String innerJob, String url, String returnedPath) throws Exception {

    when(fileDownloadService.downloadAndUngzip(url)).thenReturn(returnedPath);

    TestContext context = createTestContext(innerJob);

    RepeatStatus result = downloadTasklet.execute(context.contribution(), context.chunkContext());

    assertEquals(RepeatStatus.FINISHED, result);

    assertEquals(new ExitStatus("NO_INPUT_FILE"), context.contribution().getExitStatus());

    assertEquals("Not found", context.executionContext().getString("noFile"));

    assertEquals("NO_INPUT_FILE", context.executionContext().getString("lastDeciderStatus"));

    assertFalse(context.executionContext().containsKey("inputFile"));

    assertFalse(context.executionContext().containsKey("checksum"));

    verify(fileDownloadService).downloadAndUngzip(url);
  }

  @Test
  void shouldDoNothingWhenInnerJobIsMissing() throws Exception {

    TestContext context = createTestContext(null);

    RepeatStatus result = downloadTasklet.execute(context.contribution(), context.chunkContext());

    assertEquals(RepeatStatus.FINISHED, result);

    assertFalse(context.executionContext().containsKey("inputFile"));

    assertFalse(context.executionContext().containsKey("checksum"));

    assertFalse(context.executionContext().containsKey("noFile"));

    assertFalse(context.executionContext().containsKey("lastDeciderStatus"));

    verify(fileDownloadService, never()).downloadAndUngzip(URL_ADRESSE);

    verify(fileDownloadService, never()).downloadAndUngzip(URL_DVF);
  }

  private Path createCsvFile(String fileName) throws Exception {

    Path csvFile = temporaryDirectory.resolve(fileName);

    Files.writeString(
        csvFile,
        """
                id;nom
                1;Test
                """,
        StandardCharsets.UTF_8);

    return csvFile;
  }

  private TestContext createTestContext(String innerJob) {
    JobParametersBuilder parametersBuilder = new JobParametersBuilder();

    if (innerJob != null) {
      parametersBuilder.addString("innerJob", innerJob);
    }

    JobParameters jobParameters = parametersBuilder.toJobParameters();

    JobInstance jobInstance = new JobInstance(1L, "checkFileJob");

    JobExecution jobExecution = new JobExecution(2L, jobInstance, jobParameters);

    StepExecution stepExecution = new StepExecution(ID, "downloadStep", jobExecution);

    StepContribution contribution = stepExecution.createStepContribution();

    StepContext stepContext = new StepContext(stepExecution);

    ChunkContext chunkContext = new ChunkContext(stepContext);

    return new TestContext(contribution, chunkContext, jobExecution.getExecutionContext());
  }

  private static Stream<Arguments> downloadJobs() {
    return Stream.of(
        Arguments.of("importAdresseJob", "adresse.csv", URL_ADRESSE),
        Arguments.of("importDvfJob", "dvf.csv", URL_DVF));
  }

  private static Stream<Arguments> emptyDownloadCases() {
    return Stream.of(
        Arguments.of("importAdresseJob", URL_ADRESSE, ""),
        Arguments.of("importDvfJob", URL_DVF, "   "));
  }

  private record TestContext(
      StepContribution contribution,
      ChunkContext chunkContext,
      ExecutionContext executionContext) {}
}
