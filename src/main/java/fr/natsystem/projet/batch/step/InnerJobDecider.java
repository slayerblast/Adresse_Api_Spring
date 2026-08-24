package fr.natsystem.projet.batch.step;

import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.step.StepExecution;

public class InnerJobDecider implements JobExecutionDecider {
    private FlowExecutionStatus result = null;

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, @Nullable StepExecution stepExecution) {
        if(jobExecution.getJobParameters().getString("innerJob").equals("importAdresseJob")) {
            result = new FlowExecutionStatus("importAdresseJob");
        }
        else if(jobExecution.getJobParameters().getString("innerJob").equals("jobImportDvf")) {
            result = new FlowExecutionStatus("jobImportDvf");
        }else {
            result = new FlowExecutionStatus("importAdresseJob"); // par défaut on renvoie l'innerJob importAdresseJob
        }
        return result;
    }
}
