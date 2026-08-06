package fr.natsystem.projet.batch.step;

import fr.natsystem.projet.batch.listener.AdresseSkipListener;
import fr.natsystem.projet.batch.listener.StepProgessListener;
import fr.natsystem.projet.batch.processor.DuplicateRulesProcessor;
import fr.natsystem.projet.model.Adresse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.listener.ChunkListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcPagingItemReader;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.support.CompositeItemProcessor;
import org.springframework.batch.infrastructure.item.validator.ValidationException;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class StepConfig {

    @Value("${workerSize}")
    private int workerSize;
    @Bean
    public Step helloStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("helloStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("=== Hello, Spring Batch ! ===");
                    return RepeatStatus.FINISHED;
                }, txManager)
                .build();
    }

    @Bean
    public Step importAdresseStep(
            JobRepository repo,
            PlatformTransactionManager tx,
            JdbcPagingItemReader<Adresse> stagingReader,
            @Qualifier("jdbcWriter")
            JdbcBatchItemWriter<Adresse> jdbcWriter,
            DuplicateRulesProcessor  duplicateRulesProcessor,
            StepProgessListener listener,
            AdresseSkipListener skipListener,
            ChunkListener MetricChunkListener) {
        return new StepBuilder("importAdresseStep", repo)
                .<Adresse, Adresse>chunk(10000)
                .transactionManager(tx)
                .reader(stagingReader)
                .processor(duplicateRulesProcessor)
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
            @Qualifier("stagingWriter")
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
            SuppressionObsoleteTasklet suppressionObsoleteTasklet) {

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
            CreateStagingIndexTasklet createStagingIndexTasklet
            ) {

        return new StepBuilder("createStagingIndexStep", jobRepository)
                .tasklet(createStagingIndexTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step masterStep(JobRepository repo,
                           CodeInseePartitioner partitioner,
                           Step importAdresseStep,
                           @Qualifier("batchTaskExecutor")
                           TaskExecutor taskExecutor) {
        return new StepBuilder("masterStep", repo)
                .partitioner("importAdresseStep", partitioner)
                .step(importAdresseStep)       // step template pour chaque worker
                .gridSize(workerSize)
                .taskExecutor(taskExecutor)
                .build();
    }

    @Bean
    public Step createAdresseIndexStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            CreateIndexInterface createIndexInterface
    ) {

        return new StepBuilder("createAdresseIndexStep", jobRepository)
                .tasklet(createIndexInterface, transactionManager)
                .build();
    }
}
