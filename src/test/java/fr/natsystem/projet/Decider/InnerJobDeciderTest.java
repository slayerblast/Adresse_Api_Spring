package fr.natsystem.projet.Decider;

import fr.natsystem.projet.batch.Decider.InnerJobDecider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InnerJobDeciderTest {

    @Mock
    private JobExecution jobExecution;

    @Mock
    private JobParameters jobParameters;

    private InnerJobDecider decider;

    @BeforeEach
    void setUp() {
        decider = new InnerJobDecider();

        when(jobExecution.getJobParameters())
                .thenReturn(jobParameters);
    }

    @Test
    void shouldReturnNullWhenInnerJobParameterIsMissing() {
        when(jobParameters.getString("innerJob"))
                .thenReturn(null);

        FlowExecutionStatus result = decider.decide(
                jobExecution,
                null
        );

        assertNull(result);

        verify(jobParameters).getString("innerJob");
    }

    @ParameterizedTest(name = "[{index}] innerJob={0}, résultat attendu={1}")
    @CsvSource(
            value = {
                    "importAdresseJob | importAdresseJob",
                    "importDvfJob     | importDvfJob",
                    "unknownJob       | importAdresseJob",
                    "EMPTY            | importAdresseJob"
            },
            delimiterString = "|"
    )
    void shouldReturnExpectedJob(
            String innerJobValue,
            String expectedJob
    ) {
        String innerJob = "EMPTY".equals(innerJobValue)
                ? ""
                : innerJobValue.trim();

        when(jobParameters.getString("innerJob"))
                .thenReturn(innerJob);

        FlowExecutionStatus result = decider.decide(
                jobExecution,
                null
        );

        assertEquals(expectedJob.trim(), result.getName());

        verify(jobParameters).getString("innerJob");
    }
}