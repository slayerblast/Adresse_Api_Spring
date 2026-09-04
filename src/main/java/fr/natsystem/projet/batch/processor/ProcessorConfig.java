package fr.natsystem.projet.batch.processor;


import fr.natsystem.projet.model.Adresse;
import fr.natsystem.projet.model.AdresseValidator;
import fr.natsystem.projet.model.Dvf;
import fr.natsystem.projet.model.DvfValidator;
import org.springframework.batch.infrastructure.item.support.CompositeItemProcessor;
import org.springframework.batch.infrastructure.item.validator.BeanValidatingItemProcessor;
import org.springframework.batch.infrastructure.item.validator.ValidatingItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public ValidatingItemProcessor<Dvf> validatingProcessorDvf(DvfValidator validator) {
        return new ValidatingItemProcessor<>(validator);
    }


    @Bean
    public CompositeItemProcessor<Adresse, Adresse> compositeCsvProcessor(
            ValidatingItemProcessor<Adresse> validatingProcessor,
            BeanValidatingItemProcessor<Adresse> beanValidatingProcessor,
            DuplicateRulesProcessor duplicateRulesProcessor) {

        CompositeItemProcessor<Adresse, Adresse> comp = new CompositeItemProcessor<>();
        comp.setDelegates(List.of(
                beanValidatingProcessor,
                validatingProcessor,
                duplicateRulesProcessor
        ));
        return comp;
    }
}
