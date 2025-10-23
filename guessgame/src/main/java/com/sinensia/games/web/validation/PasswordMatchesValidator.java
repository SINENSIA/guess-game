package com.sinensia.games.web.validation;

import com.sinensia.games.web.PlayerForm;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, PlayerForm> {

    @Override
    public boolean isValid(PlayerForm value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        String password = value.getPassword();
        String confirmation = value.getPasswordConfirm();
        if (password == null || confirmation == null) {
            return false;
        }
        boolean matches = password.equals(confirmation);
        if (!matches) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("passwordConfirm")
                    .addConstraintViolation();
        }
        return matches;
    }
}
