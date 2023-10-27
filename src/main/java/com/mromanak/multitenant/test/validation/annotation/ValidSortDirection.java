package com.mromanak.multitenant.test.validation.annotation;

import com.mromanak.multitenant.test.validation.validator.SortDirectionValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Validation annotation to ensure that a string maps to one of the enumerated values of
 * {@link org.springframework.data.domain.Sort.Direction Sort.Direction}
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(
        validatedBy = {SortDirectionValidator.class}
)
public @interface ValidSortDirection {

    String message() default "must be one of the enumerated sort directions";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
