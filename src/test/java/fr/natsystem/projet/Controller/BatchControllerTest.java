package fr.natsystem.projet.Controller;

import fr.natsystem.projet.controller.BatchController;
import fr.natsystem.projet.model.JobStatusResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchControllerTest {

    @Mock
    private JobOperator jobOperator;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private Job checkFileJob;

    private BatchController controller;

    @BeforeEach
    void setUp() {
        controller = new BatchController(
                jobOperator,
                jobRepository,
                checkFileJob
        );
    }

    @Test
    void shouldStartAdresseJobWithInputFile() throws Exception {
        JobExecution jobExecution = mock(JobExecution.class);

        when(jobExecution.getId())
                .thenReturn(100L);

        when(jobOperator.start(
                same(checkFileJob),
                org.mockito.ArgumentMatchers.any(JobParameters.class)
        )).thenReturn(jobExecution);

        ResponseEntity<Object> response = controller.startJob(
                "importAdresseJob",
                "/data/adresses.csv"
        );

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals(100L, response.getBody());

        ArgumentCaptor<JobParameters> parametersCaptor =
                ArgumentCaptor.forClass(JobParameters.class);

        verify(jobOperator).start(
                same(checkFileJob),
                parametersCaptor.capture()
        );

        JobParameters parameters = parametersCaptor.getValue();

        assertAllJobParameters(
                parameters,
                "/data/adresses.csv",
                "importAdresseJob"
        );
    }

    @Test
    void shouldStartDvfJobWithInputFile() throws Exception {
        JobExecution jobExecution = mock(JobExecution.class);

        when(jobExecution.getId())
                .thenReturn(200L);

        when(jobOperator.start(
                same(checkFileJob),
                org.mockito.ArgumentMatchers.any(JobParameters.class)
        )).thenReturn(jobExecution);

        ResponseEntity<Object> response = controller.startJob(
                "importDvfJob",
                "/data/dvf.csv"
        );

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals(200L, response.getBody());

        ArgumentCaptor<JobParameters> parametersCaptor =
                ArgumentCaptor.forClass(JobParameters.class);

        verify(jobOperator).start(
                same(checkFileJob),
                parametersCaptor.capture()
        );

        JobParameters parameters = parametersCaptor.getValue();

        assertAllJobParameters(
                parameters,
                "/data/dvf.csv",
                "importDvfJob"
        );
    }

    @Test
    void shouldUseEmptyInputFileWhenRequestParameterIsNull()
            throws Exception {

        JobExecution jobExecution = mock(JobExecution.class);

        when(jobExecution.getId())
                .thenReturn(300L);

        when(jobOperator.start(
                same(checkFileJob),
                org.mockito.ArgumentMatchers.any(JobParameters.class)
        )).thenReturn(jobExecution);

        ResponseEntity<Object> response = controller.startJob(
                "importAdresseJob",
                null
        );

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals(300L, response.getBody());

        ArgumentCaptor<JobParameters> parametersCaptor =
                ArgumentCaptor.forClass(JobParameters.class);

        verify(jobOperator).start(
                same(checkFileJob),
                parametersCaptor.capture()
        );

        JobParameters parameters = parametersCaptor.getValue();

        assertAllJobParameters(
                parameters,
                "",
                "importAdresseJob"
        );
    }

    @Test
    void shouldReturnInternalServerErrorWhenJobStartFails()
            throws Exception {

        when(jobOperator.start(
                same(checkFileJob),
                org.mockito.ArgumentMatchers.any(JobParameters.class)
        )).thenThrow(
                new IllegalStateException(
                        "Impossible de démarrer le job"
                )
        );

        ResponseEntity<Object> response = controller.startJob(
                "importAdresseJob",
                "/data/adresses.csv"
        );

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode()
        );

        assertEquals(
                "Impossible de démarrer le job",
                response.getBody()
        );

        verify(jobOperator).start(
                same(checkFileJob),
                org.mockito.ArgumentMatchers.any(JobParameters.class)
        );
    }

    @Test
    void shouldReturnJobStatusWhenJobExecutionExists() {
        long jobExecutionId = 100L;

        JobExecution jobExecution = mock(JobExecution.class);
        JobInstance jobInstance = mock(JobInstance.class);

        ExecutionContext executionContext = new ExecutionContext();
        executionContext.putString("code", "94046");
        executionContext.putString("nameCode", "Maisons-Alfort");

        when(jobRepository.getJobExecution(jobExecutionId))
                .thenReturn(jobExecution);

        when(jobExecution.getId())
                .thenReturn(jobExecutionId);

        when(jobExecution.getJobInstance())
                .thenReturn(jobInstance);

        when(jobInstance.getJobName())
                .thenReturn("importAdresseJob");

        when(jobExecution.getStatus())
                .thenReturn(BatchStatus.COMPLETED);

        when(jobExecution.getExitStatus())
                .thenReturn(ExitStatus.COMPLETED);

        when(jobExecution.getExecutionContext())
                .thenReturn(executionContext);

        ResponseEntity<Object> response =
                controller.getJobStatus(jobExecutionId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        JobStatusResponse expectedResponse = new JobStatusResponse(
                jobExecutionId,
                "importAdresseJob",
                "COMPLETED",
                "COMPLETED",
                "94046",
                "Maisons-Alfort"
        );

        assertThat(response.getBody())
                .usingRecursiveComparison()
                .isEqualTo(expectedResponse);

        verify(jobRepository)
                .getJobExecution(jobExecutionId);
    }

    @Test
    void shouldUseEmptyContextValuesWhenCodeIsMissing() {
        long jobExecutionId = 101L;

        JobExecution jobExecution = mock(JobExecution.class);
        JobInstance jobInstance = mock(JobInstance.class);

        ExecutionContext executionContext = new ExecutionContext();

        when(jobRepository.getJobExecution(jobExecutionId))
                .thenReturn(jobExecution);

        when(jobExecution.getId())
                .thenReturn(jobExecutionId);

        when(jobExecution.getJobInstance())
                .thenReturn(jobInstance);

        when(jobInstance.getJobName())
                .thenReturn("importDvfJob");

        when(jobExecution.getStatus())
                .thenReturn(BatchStatus.FAILED);

        when(jobExecution.getExitStatus())
                .thenReturn(ExitStatus.FAILED);

        when(jobExecution.getExecutionContext())
                .thenReturn(executionContext);

        ResponseEntity<Object> response =
                controller.getJobStatus(jobExecutionId);

        JobStatusResponse expectedResponse = new JobStatusResponse(
                jobExecutionId,
                "importDvfJob",
                "FAILED",
                "FAILED",
                "",
                ""
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertThat(response.getBody())
                .usingRecursiveComparison()
                .isEqualTo(expectedResponse);
    }

    @Test
    void shouldReturnNotFoundWhenJobExecutionDoesNotExist() {
        long jobExecutionId = 999L;

        when(jobRepository.getJobExecution(jobExecutionId))
                .thenReturn(null);

        ResponseEntity<Object> response =
                controller.getJobStatus(jobExecutionId);

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );

        assertNull(response.getBody());

        verify(jobRepository)
                .getJobExecution(jobExecutionId);
    }

    private void assertAllJobParameters(
            JobParameters parameters,
            String expectedInputFile,
            String expectedInnerJob
    ) {
        assertNotNull(parameters);

        assertEquals(
                expectedInputFile,
                parameters.getString("inputFile")
        );

        assertEquals(
                expectedInnerJob,
                parameters.getString("innerJob")
        );

        Long startAt = parameters.getLong("startAt");

        assertNotNull(startAt);
        assertThat(startAt).isPositive();
    }
}