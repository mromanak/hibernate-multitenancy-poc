package com.mromanak.multitenant.test.config;

import com.mromanak.multitenant.test.model.error.ValidationErrorResponse;
import com.mromanak.multitenant.test.model.error.ValidationErrorResponse.ValidationError;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.util.List;

/**
 * Provides methods to handle exceptions thrown by the application.
 * <p/>
 * Currently, its only use is to reformat the two different types of validation exceptions into a consistent JSON
 * format. It's interesting to note that we hit a hitch in the Spring magic here:
 * <ul>
 *     <li>Spring does not natively perform validation on path variables or query parameters unless the controller
 *     class is annotated with {@link org.springframework.validation.annotation.Validated @Validated}</li>
 *     <li>Spring throws different exceptions for path variable and query parameter validation failures than it does
 *     for request body validation failures</li>
 *     <li>The exceptions produced by the Spring-specific
 *     {@link org.springframework.validation.annotation.Validated @Validated} annotation are framework-agnostic
 *     {@link ConstraintViolationException}s, but the exceptions produced by the framework-agnostic
 *     {@link javax.validation.Valid @Valid} are Spring-specific {@link MethodArgumentNotValidException}s/</li>
 * </ul>
 */
@ControllerAdvice
public class ExceptionHandlerConfig {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
        List<ValidationError> validationErrors = e.getConstraintViolations().stream()
                .map(cv -> new ValidationError(cv.getPropertyPath().toString(), cv.getMessage()))
                .toList();

        return ResponseEntity.badRequest().body(new ValidationErrorResponse(
                "The provided request parameters were invalid",
                validationErrors
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(MethodArgumentNotValidException e) {
        List<ValidationError> validationErrors = e.getBindingResult().getAllErrors().stream()
                .map(error -> new ValidationError(extractField(error), error.getDefaultMessage()))
                .toList();

        return ResponseEntity.badRequest().body(new ValidationErrorResponse(
                "The provided request body was invalid",
                validationErrors
        ));
    }

    public String extractField(ObjectError objectError) {

        // If there are no usable codes, just settle for the object name
        String[] codes = objectError.getCodes();
        if (codes == null || codes.length == 0) {
            return objectError.getObjectName();
        }

        /*
         * The first entry in the codes array should look like this:
         *   @Size.assetDto.name
         * Re-format that to something like this instead:
         *   requestBody.name
         */
        return codes[0].replaceFirst("^\\p{javaUpperCase}[^.]*\\.\\p{javaLowerCase}[^.]*\\.", "requestBody.");
    }
}
