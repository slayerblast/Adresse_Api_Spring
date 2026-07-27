package fr.natsystem.projet.model;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.infrastructure.item.validator.ValidationException;
import org.springframework.batch.infrastructure.item.validator.Validator;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class AdresseValidator
        implements Validator<Adresse> {

    private static final Pattern CODE_INSEE_PATTERN =
            Pattern.compile("^(?:0[1-9]|[1-8]\\d|9[0-5]|2a|2b)\\d{3}$");
    /**
     * à adapter selon le critère choisi !!!
     * Utilisé pour vérifier le format de l'id et la validation de l'INSEE
     * l'id doit commencer par le code l'INSEE ensuite je ne gère pas les caractères
     * @param adresse object to be validated
     * @throws ValidationException
     */
    @Override
    public void validate(Adresse adresse)
            throws ValidationException {

        String codeInsee = adresse.code_insee().toLowerCase(Locale.ROOT);

        if (!CODE_INSEE_PATTERN.matcher(codeInsee).matches()) {
            throw new ValidationException("Code INSEE invalide");
        }

        if (!adresse.id().startsWith(codeInsee)) {
            throw new ValidationException(
                    "L'id ne commence pas par le code INSEE : " + codeInsee);
        }

    }
}
