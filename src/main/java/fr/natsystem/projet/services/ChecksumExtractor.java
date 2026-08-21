package fr.natsystem.projet.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.step.job.JobParametersExtractor;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChecksumExtractor implements JobParametersExtractor {

    @Override
    public JobParameters getJobParameters(Job job, StepExecution stepExecution) {

        String checksum = stepExecution
                .getJobExecution()
                .getExecutionContext()
                .getString("checksum", "");
        String exitStatus = stepExecution
                .getJobExecution()
                .getExecutionContext()
                .getString("lastDeciderStatus", "");
        log.info("checksum dans le getJobParameters = {}",checksum);
        return new JobParametersBuilder()
                .addString("checksum", checksum,true)
                .addString("lastExitStatus", exitStatus)
                .addLong("jobExecutionId",stepExecution.getJobExecution().getId())
                .toJobParameters();
    }
}