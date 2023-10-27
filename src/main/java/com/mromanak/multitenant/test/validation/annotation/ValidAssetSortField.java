package com.mromanak.multitenant.test.validation.annotation;

import com.mromanak.multitenant.test.validation.validator.AssetSortFieldValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Validation annotation to ensure that a string maps to one of the enumerated values of
 * {@link com.mromanak.multitenant.test.model.entity.Asset.SortField Asset.SortField}
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(
        validatedBy = {AssetSortFieldValidator.class}
)
public @interface ValidAssetSortField {

    String message() default "must be one of the enumerated sort fields for an Asset";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
