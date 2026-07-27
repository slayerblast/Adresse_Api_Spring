package fr.natsystem.projet.batch.processor;

import fr.natsystem.projet.model.Adresse;
import fr.natsystem.projet.model.AdresseValidator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.item.support.CompositeItemProcessor;
import org.springframework.batch.infrastructure.item.validator.BeanValidatingItemProcessor;
import org.springframework.batch.infrastructure.item.validator.ValidatingItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
public class ProcessorConfig {

    @Bean
    public BeanValidatingItemProcessor<Adresse> beanValidatingProcessor() {
        BeanValidatingItemProcessor<Adresse> p = new BeanValidatingItemProcessor<>();
        p.setFilter(true);
        return p;
    }

    @Bean
    public ValidatingItemProcessor<Adresse> validatingProcessor(AdresseValidator validator) {
        return new ValidatingItemProcessor<>(validator);
    }

    @Bean
    public CompositeItemProcessor<Adresse, Adresse> compositeProcessor(
            DuplicateRulesProcessor duplicateRulesProcessor) {

        CompositeItemProcessor<Adresse, Adresse> comp = new CompositeItemProcessor<>();
        comp.setDelegates(List.of( duplicateRulesProcessor));
        return comp;
    }

    @Bean
    public CompositeItemProcessor<Adresse, Adresse> compositeCsvProcessor(
            ValidatingItemProcessor<Adresse> validatingProcessor,
            BeanValidatingItemProcessor<Adresse> beanValidatingProcessor) {

        CompositeItemProcessor<Adresse, Adresse> comp = new CompositeItemProcessor<>();
        comp.setDelegates(List.of(
                beanValidatingProcessor,
                validatingProcessor
        ));
        return comp;
    }
}
