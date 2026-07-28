package fr.natsystem.projet;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ProjetApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjetApplication.class, args);
    }

    // Pour lancer le job au démarrage (mode démo) :
    @Bean
    public CommandLineRunner run(JobOperator launcher, Job helloWorldJob, Job importAdresseJob) {
        return args -> {
            //On ajoute cette condition car cela faisait planter le build de test
            if(args.length==0){
                return;
            }
            JobParameters params = new JobParametersBuilder()
                    .addString("inputFile", args[0])
                    .addLong("startAt", System.currentTimeMillis())
                    .toJobParameters();
            launcher.start(helloWorldJob, params);
            launcher.start(importAdresseJob, params);


        };
    }
}
