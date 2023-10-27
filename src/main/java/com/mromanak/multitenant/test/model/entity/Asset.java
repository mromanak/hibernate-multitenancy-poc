package com.mromanak.multitenant.test.model.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import com.mromanak.multitenant.test.model.Identifiable;
import com.mromanak.multitenant.test.validation.ValidationUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

/**
 * An Asset entity
 * <p/>
 * I picked a generic name and some fields that are a bit more complex than primitives or strings
 * <p/>
 * TODO: Implement a geometry column
 */
@Getter
@Setter
@ToString
@Entity
@Schema(
        name = Asset.REF,
        description = "The response format used when describing an Asset",
        additionalProperties = Schema.AdditionalPropertiesValue.FALSE,
        example = Asset.EXAMPLE_OBJECT
)
public class Asset implements Identifiable<UUID> {
    
    public static final String REF = "AssetResponse";

    public static final String EXAMPLE_OBJECT = """
            {
                "id": "00000000-0000-0000-0000-000000000000",
                "name": "Example AssetResponse",
                "createdTimestamp": "1970-01-01T00:00:00.000Z",
                "serverUrl": "https://example.server.com/"
            }""";

    /**
     * An enumeration of the fields an Asset may be sorted by. Configured to use the java name of the field for
     * serialization and deserialization
     */
    public enum SortField {
        ID("id"),
        NAME("name"),
        CREATED_TIMESTAMP("createdTimestamp");

        private static final Map<String, SortField> DISPLAY_VALUE_TO_SORT_FIELD_MAP;

        static {
            DISPLAY_VALUE_TO_SORT_FIELD_MAP = Stream.of(values())
                    .collect(toMap(SortField::getFieldName, Function.identity()));
        }

        private final String fieldName;

        SortField(String fieldName) {
            this.fieldName = fieldName;
        }

        @JsonCreator
        public static SortField fromString(String fieldName) {
            return DISPLAY_VALUE_TO_SORT_FIELD_MAP.get(fieldName);
        }

        @JsonValue
        public String getFieldName() {
            return fieldName;
        }
    }

    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false, unique = true)
    @Schema(
            type = "string",
            description = "The unique identifier of the Asset",
            pattern = ValidationUtils.UUID_PATTERN,
            minLength = 36,
            maxLength = 36
    )
    @NotNull
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    @Schema(description = "The unique name of the Asset")
    @NotEmpty
    @Size(min = 1, max = 50)
    @Pattern(regexp = ValidationUtils.NAME_PATTERN)
    private String name;

    @Column(name = "created_timestamp", columnDefinition = "TIMESTAMP WITH TIMEZONE", nullable = false)
    @Temporal(value= TemporalType.TIMESTAMP)
    @Schema(
            type = "string",
            description = "The ISO-8601 timestamp at which the Asset was created",
            format = "date-time"
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ValidationUtils.ISO_8601_DATE_FORMAT)
    @NotNull
    private Date createdTimestamp;

    @Schema(
            type = "string",
            description = "The URL of the Asset's server",
            pattern = ValidationUtils.HTTPS_URL_PATTERN,
            maxLength = 1000
    )
    @Column(name = "server_url")
    private URL serverUrl;
}
