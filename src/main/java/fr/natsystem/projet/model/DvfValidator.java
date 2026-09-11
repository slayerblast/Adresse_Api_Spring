package fr.natsystem.projet.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.validator.ValidationException;
import org.springframework.batch.infrastructure.item.validator.Validator;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DvfValidator implements Validator<Dvf> {
  @Override
  public void validate(Dvf value) throws ValidationException {
    if (value.latitude() == 0.0 || value.longitude() == 0.0) {
      log.info("Cordonnée invalide");
      throw new ValidationException("La distance n'est pas valide");
    }
  }
}
