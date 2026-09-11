package fr.natsystem.projet.batch.job;

import fr.natsystem.projet.batch.Decider.CheckArgDecider;
import fr.natsystem.projet.batch.Decider.FileCountDecider;
import fr.natsystem.projet.batch.Decider.InnerJobDecider;
import fr.natsystem.projet.batch.listener.BilanJobListener;
import fr.natsystem.projet.batch.listener.CheckFileListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.EnableJdbcJobRepository;
import org.springframework.batch.core.job.Job;
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
  public Job importAdresseJob(
      JobRepository jobRepository,
      Step masterStepAdresse,
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
        .next(masterStepAdresse)
        .next(suppressionObsoleteStep)
        .next(createAdresseIndexStep)
        .next(checksumStep)
        .build();
  }

  @Bean
  public Job importDvfJob(
      JobRepository jobRepository,
      Step masterStepDvf,
      BilanJobListener listener,
      Step createStagingIndexStep,
      Step dvfSqlRequestStep,
      Step csvToStagingStep,
      Step checksumStep) {
    return new JobBuilder("importDvfJob", jobRepository)
        .listener(listener)
        .start(csvToStagingStep)
        .next(createStagingIndexStep)
        .next(masterStepDvf)
        .next(checksumStep)
        .next(dvfSqlRequestStep)
        .build();
  }

  @Bean
  public Job checkFileJob(
      JobRepository jobRepository,
      FileCountDecider fileCountDecider,
      CheckArgDecider checkArgDecider,
      InnerJobDecider innerJobDecider,
      CheckFileListener listener,
      Step dvfJobStep,
      Step downloadStep,
      Step adresseJobStep) {
    final String NO_INPUT_FILE = "NO_INPUT_FILE";
    final String OK_ARG_NOT_EMPTY = "OK_ARG_NOT_EMPTY";
    final String MULTIPLE_FILES_FOUND = "MULTIPLE_FILES_FOUND";
    final String OK_FOR_RETRIEVE = "OK_FOR_RETRIEVE";
    final String OK_FOR_IMPORT = "OK_FOR_IMPORT";

    return new JobBuilder("checkFileJob", jobRepository)
        .start(fileCountDecider)
        .on(OK_ARG_NOT_EMPTY)
        .to(checkArgDecider)
        .from(fileCountDecider)
        .on(MULTIPLE_FILES_FOUND)
        .fail()
        .from(fileCountDecider)
        .on(NO_INPUT_FILE)
        .end()
        .from(fileCountDecider)
        .on(OK_FOR_RETRIEVE)
        .to(downloadStep)
        .from(fileCountDecider)
        .on(OK_FOR_IMPORT)
        .to(innerJobDecider)
        .from(checkArgDecider)
        .on(NO_INPUT_FILE)
        .end()
        .from(checkArgDecider)
        .on(MULTIPLE_FILES_FOUND)
        .fail()
        .from(checkArgDecider)
        .on(OK_FOR_RETRIEVE)
        .to(downloadStep)
        .from(checkArgDecider)
        .on("OK_FILE_EXIST")
        .to(innerJobDecider)
        .from(downloadStep)
        .on(NO_INPUT_FILE)
        .end()
        .from(downloadStep)
        .on("READY")
        .to(innerJobDecider)
        .from(innerJobDecider)
        .on("importAdresseJob")
        .to(adresseJobStep)
        .from(innerJobDecider)
        .on("importDvfJob")
        .to(dvfJobStep)
        .end()
        .listener(listener)
        .build();
  }
}
