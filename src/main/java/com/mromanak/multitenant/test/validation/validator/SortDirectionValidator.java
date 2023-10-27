package com.mromanak.multitenant.test.validation.validator;

import com.mromanak.multitenant.test.validation.annotation.ValidSortDirection;
import org.springframework.data.domain.Sort;

import java.util.Optional;

/**
 * A {@link javax.validation.ConstraintValidator} that checks whether a string maps to one of the enumerated values of
 * {@link Sort.Direction}
 */
public class SortDirectionValidator extends AbstractEnumValidator<ValidSortDirection, Sort.Direction> {

    @Override
    protected Optional<Sort.Direction> parseEnum(String value) {
        try {
            return Optional.of(Sort.Direction.fromString(value));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
