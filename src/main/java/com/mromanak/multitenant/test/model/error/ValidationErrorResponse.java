package com.mromanak.multitenant.test.model.error;

import com.mromanak.multitenant.test.validation.ValidationUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * An error response with a general message and a list of more specific validation error messages
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(
        name = ValidationErrorResponse.REF,
        description = "A description of an error that occurred while validating the content of a request",
        additionalProperties = Schema.AdditionalPropertiesValue.FALSE,
        example = ValidationErrorResponse.EXAMPLE_OBJECT
)
public class ValidationErrorResponse extends ErrorResponse {

    public static final String REF = "ValidationErrorResponse";
    public static final String EXAMPLE_OBJECT = """
            {
                "errorMessage": "The provided request body was invalid",
                "validationErrors": [
                    {
                        "field": "responseBody.id",
                        "message": "The ID was invalid"
                    }
                ]
            }""";

    public ValidationErrorResponse(String errorMessage, List<ValidationError> validationErrors) {
        super(errorMessage);
        this.validationErrors = validationErrors;
    }

    /**
     * A specific validation error describing the nature of the error and invalid field
     */
    @Data
    @AllArgsConstructor
    @Schema(
            name = ValidationError.REF,
            description = "The location and detail of a single error found during request validation",
            additionalProperties = Schema.AdditionalPropertiesValue.FALSE,
            example = ValidationError.EXAMPLE_OBJECT
    )
    public static class ValidationError {

        public static final String REF = "ValidationError";
        public static final String EXAMPLE_OBJECT = """
                {
                    "field": "responseBody.id",
                    "message": "The ID was invalid"
                }""";

        @Schema(description = "The location in the request at which this validation error occurred")
        @Pattern(regexp = ValidationUtils.SIMPLE_TEXT_PATTERN)
        @Size(min = 1, max = 10_000)
        private String field;

        @Schema(description = "A description of this validation error")
        @Pattern(regexp = ValidationUtils.SIMPLE_TEXT_PATTERN)
        @Size(min = 1, max = 10_000)
        private String message;
    }

    private List<ValidationError> validationErrors;
}
