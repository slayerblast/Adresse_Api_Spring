package fr.natsystem.projet.batch.Decider;

import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

@Component
public class InnerJobDecider implements JobExecutionDecider {

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, @Nullable StepExecution stepExecution) {
        final String innerJob = "innerJob";
        final String importAdresseJob = "importAdresseJob";
        final String importDvfJob = "importDvfJob";
        FlowExecutionStatus result = null;
        String jobParams = jobExecution.getJobParameters().getString(innerJob);
        if (jobParams == null) {
            return result;
        }
        if (jobParams.equals(importAdresseJob)) {
            result = new FlowExecutionStatus(importAdresseJob);
        } else if (jobParams.equals(importDvfJob)) {
            result = new FlowExecutionStatus(importDvfJob);
        } else {
            result = new FlowExecutionStatus(importAdresseJob); // par défaut on renvoie l'innerJob importAdresseJob
        }

        return result;
    }
}
