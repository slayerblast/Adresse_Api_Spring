package fr.natsystem.projet.batch.step;

import fr.natsystem.projet.batch.listener.AdresseSkipListener;
import fr.natsystem.projet.batch.listener.StepProgessListener;
import fr.natsystem.projet.model.Adresse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.listener.ChunkListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcPagingItemReader;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.support.CompositeItemProcessor;
import org.springframework.batch.infrastructure.item.validator.ValidationException;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class StepConfig {
    @Bean
    public Step helloStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("helloStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("=== Hello, Spring Batch ! ===");
                    return RepeatStatus.FINISHED;
                }, txManager)
                .build();
    }
    int test = 0;
    @Bean
    public Step importAdresseStep(
            JobRepository repo,
            PlatformTransactionManager tx,
            JdbcPagingItemReader<Adresse> stagingReader,
            JdbcBatchItemWriter<Adresse> jdbcWriter,
            CompositeItemProcessor<Adresse, Adresse> compositeProcessor,
            StepProgessListener listener,
            AdresseSkipListener skipListener,
            ChunkListener MetricChunkListener) {
        return new StepBuilder("importAdresseStep", repo)
                .<Adresse, Adresse>chunk(10000)
                .transactionManager(tx)
                .reader(stagingReader)
                .processor(compositeProcessor)
                .writer(jdbcWriter)
                .faultTolerant()
                .skip(ValidationException.class)
                .skipLimit(Integer.MAX_VALUE)
                .listener(listener)
                .listener(skipListener)
                .listener(MetricChunkListener)
                .build();

    }
    @Bean
    public Step importCsvStep(
            JobRepository repo,
            PlatformTransactionManager tx,
            FlatFileItemReader<Adresse> csvReader,
            JdbcBatchItemWriter<Adresse> stagingWriter,
            CompositeItemProcessor <Adresse, Adresse> compositeCsvProcessor,
            StepProgessListener listener,
            AdresseSkipListener skipListener) {
        return new StepBuilder("importCsvStep", repo)
                .<Adresse, Adresse>chunk(10000)
                .transactionManager(tx)
                .reader(csvReader)
                .processor(compositeCsvProcessor)
                .writer(stagingWriter)
                .faultTolerant()
                .skip(ValidationException.class)
                .skipLimit(Integer.MAX_VALUE)
                .listener(listener)
                .listener(skipListener)
                .build();
    }

    @Bean
    public Step suppressionObsoleteStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            Tasklet suppressionObsoleteTasklet) {

        return new StepBuilder(
                "suppressionObsoleteStep",
                jobRepository)
                .tasklet(
                        suppressionObsoleteTasklet,
                        transactionManager)
                .build();
    }

    @Bean
    public Step createStagingIndexStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            Tasklet createStagingIndexTasklet
            ) {

        return new StepBuilder("createStagingIndexStep", jobRepository)
                .tasklet(createStagingIndexTasklet, transactionManager)
                .build();
    }
}
