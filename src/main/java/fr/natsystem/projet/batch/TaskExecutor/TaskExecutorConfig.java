package fr.natsystem.projet.batch.TaskExecutor;

import org.springframework.batch.core.launch.support.JobOperatorFactoryBean;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class TaskExecutorConfig {
    @Value("${workerSize}")
    private int workerSize;

    @Bean("batchTaskExecutor")
    @Profile("sqlite")
    public TaskExecutor sqliteTaskExecutor() {
        return new SyncTaskExecutor();
    }

    @Bean("batchTaskExecutor")
    @Profile("postgres")
    public TaskExecutor postgresTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(workerSize);
        executor.setMaxPoolSize(workerSize);
        executor.setThreadNamePrefix("batch-");
        executor.initialize();
        return executor;
    }


    @Bean("JobTaskExecutor")
    @Primary
    public JobOperatorFactoryBean asyncJobOperator(JobRepository jobRepository) {
        JobOperatorFactoryBean jobOperatorFactoryBean = new JobOperatorFactoryBean();
        jobOperatorFactoryBean.setJobRepository(jobRepository);
        jobOperatorFactoryBean.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return jobOperatorFactoryBean;
    }
}
