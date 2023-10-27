package com.mromanak.multitenant.test.validation.validator;

import com.mromanak.multitenant.test.model.entity.Asset;
import com.mromanak.multitenant.test.validation.annotation.ValidAssetSortField;

import java.util.Optional;

/**
 * A {@link javax.validation.ConstraintValidator} that checks whether a string maps to one of the enumerated values of
 * {@link com.mromanak.multitenant.test.model.entity.Asset.SortField Asset.SortField}
 */
public class AssetSortFieldValidator extends AbstractEnumValidator<ValidAssetSortField, Asset.SortField> {

    @Override
    protected Optional<Asset.SortField> parseEnum(String value) {
        return Optional.ofNullable(Asset.SortField.fromString(value));
    }
}
