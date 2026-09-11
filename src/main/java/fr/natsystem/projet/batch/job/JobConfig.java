package fr.natsystem.projet.batch.job;

import fr.natsystem.projet.batch.Decider.CheckArgDecider;
import fr.natsystem.projet.batch.Decider.FileCountDecider;
import fr.natsystem.projet.batch.Decider.InnerJobDecider;
import fr.natsystem.projet.batch.listener.BilanJobListener;
import fr.natsystem.projet.batch.listener.CheckFileListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.EnableJdbcJobRepository;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
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
      Flow importFlow,
      BilanJobListener listener,
      Step suppressionObsoleteStep,
      Step createAdresseIndexStep,
      Step checksumStep) {
    return new JobBuilder("importAdresseJob", jobRepository)
        .listener(listener)
        .start(importFlow)
        .next(suppressionObsoleteStep)
        .next(createAdresseIndexStep)
        .next(checksumStep)
        .end()
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
      CheckFileListener listener,
      Step downloadStep,
      Flow importInnerFlow) {

    final String noInputFile = "NO_INPUT_FILE";
    final String okArgNotEmpty = "OK_ARG_NOT_EMPTY";
    final String multipleFilesFound = "MULTIPLE_FILES_FOUND";
    final String okForRetrieve = "OK_FOR_RETRIEVE";
    final String okForImport = "OK_FOR_IMPORT";
    final String okFileExist = "OK_FILE_EXIST";
    final String ready = "READY";

    return new JobBuilder("checkFileJob", jobRepository)
        .start(fileCountDecider)
        .on(okArgNotEmpty)
        .to(checkArgDecider)
        .from(fileCountDecider)
        .on(multipleFilesFound)
        .fail()
        .from(fileCountDecider)
        .on(noInputFile)
        .end()
        .from(fileCountDecider)
        .on(okForRetrieve)
        .to(downloadStep)
        .from(fileCountDecider)
        .on(okForImport)
        .to(importInnerFlow)
        .from(checkArgDecider)
        .on(noInputFile)
        .end()
        .from(checkArgDecider)
        .on(multipleFilesFound)
        .fail()
        .from(checkArgDecider)
        .on(okForRetrieve)
        .to(downloadStep)
        .from(checkArgDecider)
        .on(okFileExist)
        .to(importInnerFlow)
        .from(downloadStep)
        .on(noInputFile)
        .end()
        .from(downloadStep)
        .on(ready)
        .to(importInnerFlow)
        .end()
        .listener(listener)
        .build();
  }

  @Bean
  public Flow importFlow(
      Step createAdresseIndexStep, Step csvToStagingStep, Step masterStepAdresse) {
    return new FlowBuilder<Flow>("importFlow")
        .start(csvToStagingStep)
        .next(createAdresseIndexStep)
        .next(masterStepAdresse)
        .build();
  }

  @Bean
  public Flow importInnerFlow(
      InnerJobDecider innerJobDecider, Step dvfJobStep, Step adresseJobStep) {

    return new FlowBuilder<Flow>("importInnerFlow")
        .start(innerJobDecider)
        .on("importAdresseJob")
        .to(adresseJobStep)
        .from(innerJobDecider)
        .on("importDvfJob")
        .to(dvfJobStep)
        .end();
  }
}
