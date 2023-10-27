package com.mromanak.multitenant.test.model.error;

import com.mromanak.multitenant.test.validation.ValidationUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * A generic error response with a single message
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(
        name = ErrorResponse.REF,
        description = "A description of an error that occurred while processing a request",
        additionalProperties = Schema.AdditionalPropertiesValue.FALSE,
        example = ErrorResponse.EXAMPLE_OBJECT,
        subTypes = {
                ValidationErrorResponse.class
        }
)
public class ErrorResponse {

    public static final String REF = "ErrorResponse";
    public static final String EXAMPLE_OBJECT = """
            {
                "errorMessage": "Something has gone wrong"
            }""";

    @Schema(description = "A description of the error")
    @Pattern(regexp = ValidationUtils.SIMPLE_TEXT_PATTERN)
    @Size(min = 1, max = 10_000)
    private String errorMessage;
}
