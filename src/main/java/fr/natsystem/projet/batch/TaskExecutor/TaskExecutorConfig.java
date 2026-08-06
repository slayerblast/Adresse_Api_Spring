package fr.natsystem.projet.batch.TaskExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@Configuration
public class TaskExecutorConfig {

    @Bean("batchTaskExecutor")
    @Profile("sqlite")
    public TaskExecutor sqliteTaskExecutor() {
        return new SyncTaskExecutor();
    }

    @Bean("batchTaskExecutor")
    @Profile("postgres")
    public TaskExecutor PostgresTaskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }
}
