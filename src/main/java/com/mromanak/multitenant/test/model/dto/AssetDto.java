package com.mromanak.multitenant.test.model.dto;

import com.mromanak.multitenant.test.model.DtoFor;
import com.mromanak.multitenant.test.model.entity.Asset;
import com.mromanak.multitenant.test.validation.ValidationUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * A DTO class for an {@link Asset}
 */
@Data
@Schema(
        name = AssetDto.REF,
        description = "The request format used to create or update an Asset",
        additionalProperties = Schema.AdditionalPropertiesValue.FALSE,
        example = AssetDto.EXAMPLE_OBJECT
)
public class AssetDto implements DtoFor<java.util.UUID, Asset> {

    public static final String REF = "AssetRequest";

    public static final String EXAMPLE_OBJECT = """
            {
                "name": "Example AssetRequest",
                "createdTimestamp": "1970-01-01T00:00:00.000Z",
                "serverUrl": "https://example.server.com/"
            }""";

    @Schema(description = "The unique name of the Asset")
    @NotEmpty
    @Size(min = 1, max = 50)
    @Pattern(regexp = ValidationUtils.NAME_PATTERN)
    private String name;

    @Schema(
            description = "The ISO-8601 timestamp at which the Asset was created",
            format = "date-time"
    )
    @NotEmpty
    @Pattern(
            regexp = ValidationUtils.ISO_8601_DATE_PATTERN,
            message = "must be a valid ISO-8601 date-time"
    )
    private String createdTimestamp;

    @Schema(
            description = "The URL of the Asset's server",
            pattern = ValidationUtils.HTTPS_URL_PATTERN
    )
    @Size(max = 1000)
    @URL(protocol = "https")
    private String serverUrl;
}
