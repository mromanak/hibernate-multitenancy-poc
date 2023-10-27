package com.mromanak.multitenant.test.validation.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;
import java.util.Optional;

/**
 * Implements the skeleton of a {@link ConstraintValidator} that ensures that a string maps to an enumerated value of an
 * {@link Enum}.
 *
 * @param <A> The validation annotation that this validator implements
 * @param <E> The enum type
 */
public abstract class AbstractEnumValidator<A extends Annotation, E extends Enum<E>> implements
        ConstraintValidator<A, String> {

    /**
     * Maps a string to an enum value
     *
     * @param value The string value to be validated
     * @return An optional containing the enum value, if {@code value} valid, or an empty optional otherwise
     */
    protected abstract Optional<E> parseEnum(String value);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) {
            return true;
        }

        return parseEnum(value).isPresent();
    }
}
