package fr.natsystem.projet.batch.listener;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

@Component
public class NestedJobStepListener implements StepExecutionListener {

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {

        stepExecution.getJobExecution().getExecutionContext().putString("code", "202");
        stepExecution.getJobExecution().getExecutionContext().putString( "nameCode", "Nouvelle exécution acceptée et démarrée");
        for (Throwable exception : stepExecution.getJobExecution().getAllFailureExceptions()) {

            if (exception instanceof JobInstanceAlreadyCompleteException) {

                stepExecution.getJobExecution().getExecutionContext().putString("code", "409");
                stepExecution.getJobExecution().getExecutionContext().putString("nameCode", "JobInstanceAlreadyCompleteException");
            }

            if (exception instanceof JobExecutionAlreadyRunningException) {

                stepExecution.getJobExecution().getExecutionContext().putString("code", "432");
                stepExecution.getJobExecution().getExecutionContext().putString("nameCode", "JobExecutionAlreadyRunningException");
            }
        }

        return stepExecution.getExitStatus();
    }
}