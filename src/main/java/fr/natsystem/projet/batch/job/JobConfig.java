package fr.natsystem.projet.batch.job;

import fr.natsystem.projet.batch.listener.checkFileListener;
import fr.natsystem.projet.batch.step.CheckArgDecider;
import fr.natsystem.projet.batch.step.FileCountDecider;
import fr.natsystem.projet.batch.listener.BilanJobListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.EnableJdbcJobRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
@EnableBatchProcessing
@EnableJdbcJobRepository
public class JobConfig {
    @Bean
    public Job helloWorldJob(JobRepository jobRepository, Step helloStep) {
        return new JobBuilder("helloWorldJob", jobRepository).start(helloStep).build();
    }

    @Bean
    public Job importAdresseJob(JobRepository jobRepository, Step masterStep,
                                BilanJobListener listener,
                                Step createStagingIndexStep,
                                Step suppressionObsoleteStep,
                                Step createAdresseIndexStep,
                                Step csvToStagingStep,
                                Step checksumStep) {
        return new JobBuilder("importAdresseJob", jobRepository)
                .listener(listener)
                .start(csvToStagingStep)
                .next(createStagingIndexStep)
                .next(masterStep)
                .next(suppressionObsoleteStep)
                .next(createAdresseIndexStep)
                .next(checksumStep)
                .build();
    }


    @Bean
    public Job checkFileJob(JobRepository jobRepository,
                            FileCountDecider fileCountDecider,
                            CheckArgDecider checkArgDecider,
                            checkFileListener Listener,
                            Step downloadStep,
                            Step nestedJobStep) {
        return new JobBuilder("checkFileJob", jobRepository)
                .start(fileCountDecider)
                .on("OK_ARG_NOT_EMPTY")
                .to(checkArgDecider)

                .from(checkArgDecider)
                .on("NO_INPUT_FILE").end()

                .from(checkArgDecider)
                .on("MULTIPLE_FILES_FOUND").fail()

                .from(checkArgDecider)
                .on("OK_FOR_RETRIEVE").to(downloadStep).next(nestedJobStep)

                .from(checkArgDecider)
                .on("OK_FILE_EXIST")
                .to(nestedJobStep)

                .from(fileCountDecider)
                .on("MULTIPLE_FILES_FOUND").fail()

                .from(fileCountDecider)
                .on("NO_INPUT_FILE").end()

                .from(fileCountDecider)
                .on("OK_FOR_RETRIEVE").to(downloadStep)

                .from(fileCountDecider)
                .on("OK_FOR_IMPORT").to(nestedJobStep)

                .from(downloadStep)
                .on("NO_INPUT_FILE").end()
                .from(downloadStep)
                .on("READY").to(nestedJobStep)

                .end()
                .listener(Listener)
                .build();
    }
   /*
    @Bean
    public Job importAdresseJob(JobRepository jobRepository,
                                BilanJobListener listener,
                                Flow importFlow,
                                FileCountDecider fileCountDecider,
                                CheckArgDecider checkArgDecider,
                                Step downloadStep) {
        return new JobBuilder("importAdresseJob", jobRepository)
                .listener(listener)
                .start(fileCountDecider)
                    .on("OK_ARG_NOT_EMPTY")
                        .to(checkArgDecider)

                .from(checkArgDecider)
                    .on("NO_INPUT_FILE").end()

                .from(checkArgDecider)
                    .on("MULTIPLE_FILES_FOUND").fail()

                .from(checkArgDecider)
                    .on("OK_FOR_RETRIEVE").to(downloadStep).next(importFlow)

                .from(checkArgDecider)
                        .on("OK_FILE_EXIST")
                                .to(importFlow)

                .from(fileCountDecider)
                    .on("MULTIPLE_FILES_FOUND").fail()

                .from(fileCountDecider)
                    .on("NO_INPUT_FILE").end()

                .from(fileCountDecider)
                    .on("OK_FOR_RETRIEVE").to(downloadStep)

                .from(fileCountDecider)
                    .on("OK_FOR_IMPORT").to(importFlow)

                .from(downloadStep)
                    .on("NO_INPUT_FILE").end()
                .from(downloadStep)
                    .on("READY").to(importFlow)

                .end()
                .build();
    }

    @Bean
    public Flow importFlow(Step createStagingIndexStep,
                           Step masterStep,
                           Step suppressionObsoleteStep,
                           Step csvToStagingStep,
                           Step createAdresseIndexStep,
                           Step checksumStep) {
        return new FlowBuilder<Flow>("importFlow")
                .start(csvToStagingStep)
                .next(createStagingIndexStep)
                .next(masterStep)
                .next(suppressionObsoleteStep)
                .next(createAdresseIndexStep)
                .next(checksumStep)
                .build();
    }
*/

}
