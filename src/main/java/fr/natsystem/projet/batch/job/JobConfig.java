package fr.natsystem.projet.batch.job;

import fr.natsystem.projet.batch.listener.BilanJobListener;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobConfig {
    @Bean
    public Job helloWorldJob(JobRepository jobRepository, Step helloStep) {
        return new JobBuilder("helloWorldJob", jobRepository).start(helloStep).build();
    }

    @Bean
    public Job importAdresseJob(JobRepository jobRepository, Step importAdresseStep,
                                BilanJobListener listener,
                                Step createStagingIndexStep,
                                Step suppressionObsoleteStep,
                                Step importCsvStep) {
        return new JobBuilder("importAdresseJob", jobRepository)
                .listener(listener)
                .start(importCsvStep)
                .next(createStagingIndexStep)
                .next(importAdresseStep)
                .next(suppressionObsoleteStep)
                .build();
    }

}
