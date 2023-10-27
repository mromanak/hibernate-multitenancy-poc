package com.mromanak.multitenant.test.controller;

import com.mromanak.multitenant.test.model.dto.AssetDto;
import com.mromanak.multitenant.test.model.entity.Asset;
import com.mromanak.multitenant.test.openapi.GenericApiErrorResponses;
import com.mromanak.multitenant.test.service.EntityCrudService;
import com.mromanak.multitenant.test.validation.ValidationUtils;
import com.mromanak.multitenant.test.validation.annotation.ValidAssetSortField;
import com.mromanak.multitenant.test.validation.annotation.ValidSortDirection;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Collections;
import java.util.UUID;

/**
 * A controller that performs basic CRUD operations for {@link Asset Assets}.
 * <p/>
 * A couple of things to note:
 * <ul>
 *     <li>This controller attempts to accept simple objects that are easier to validate (e.g. strings instead of UUIDs
 *     or enums) and then convert them after they've been validated. By funneling everything through the validators
 *     first, we more consistency and better user-readability of error responses.</li>
 *     <li>When we use {@link Secured @Secured} to require OIDC roles to access an endpoint, the default code that
 *     extracts authorities from the JWT pulls them from {@code realm_access.roles} and adds the prefix
 *     {@code ROLE_}</li></li>
 * </ul>
 * <p/>
 * TODO: Figure out how to generate an OpenAPI schema for a {@link org.springframework.data.domain.Page}
 */
@RestController
@RequestMapping("/asset")
@Validated
@Slf4j
public class AssetController extends AbstractEntityCrudController<UUID, AssetDto, Asset> {

    @Autowired
    protected AssetController(EntityCrudService<UUID, AssetDto, Asset> service) {
        super(service);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected String getEntityClassName() {
        return "Asset";
    }

    @Override
    protected java.util.UUID parseIdString(String idString) {
        return java.util.UUID.fromString(idString);
    }

    @Secured("ROLE_write:asset")
    @RequestMapping(
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Creates a new Asset")
    @SecurityRequirement(
            name = "OIDC",
            scopes = "write:access"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The requested asset was created successfully",
                    content = @Content(
                            schema = @Schema(implementation = Asset.class)
                    )
            )
    })
    public ResponseEntity<?> createAsset(
            @RequestBody
            @Valid
            @NotNull(message = "A request body must be provided")
            AssetDto asset
    ) {
        return create(asset);
    }

    @Secured("ROLE_read:asset")
    @RequestMapping(
            path = "/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Retrieves an Asset")
    @SecurityRequirement(
            name = "OIDC",
            scopes = "read:access"
    )
    @ApiResponse(
            responseCode = "200",
            description = "The requested asset was retrieved successfully",
            content = @Content(
                    schema = @Schema(implementation = Asset.class)
            )
    )
    @GenericApiErrorResponses
    public ResponseEntity<?> getAsset(
            @PathVariable("id")
            @Schema(
                    description = "The ID of the Asset to be retrieved",
                    minLength = 36,
                    maxLength = 36,
                    example = ValidationUtils.UUID_EXAMPLE_OBJECT
            )
            @Pattern(
                    regexp = ValidationUtils.UUID_PATTERN,
                    flags = Pattern.Flag.CASE_INSENSITIVE,
                    message = "must be a valid UUID"
            )
            String id
    ) {
        return read(parseIdString(id));
    }

    @Secured("ROLE_read:asset")
    @RequestMapping(
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Retrieves a page of Assets")
    @SecurityRequirement(
            name = "OIDC",
            scopes = "read:access"
    )
    @ApiResponse(
            responseCode = "200",
            description = "The requested asset was retrieved successfully",
            content = @Content(
                    array = @ArraySchema(
                            arraySchema = @Schema(implementation =  Page.class),
                            schema = @Schema(implementation = Asset.class)
                    )
            )
    )
    @GenericApiErrorResponses
    public ResponseEntity<?> getAssetPage(
            @RequestParam(name = "pageNumber", defaultValue = "0")
            @Max(Integer.MAX_VALUE)
            @Min(0)
            Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "25")
            @Max(100)
            @Min(0)
            Integer pageSize,
            @RequestParam(name = "sortField", required = false)
            @Parameter(
                    schema = @Schema(
                            implementation = Asset.SortField.class,
                            enumAsRef = true
                    )
            )
            @ValidAssetSortField
            String sortField,
            @RequestParam(name = "sortDirection", defaultValue = "asc")
            @Parameter(
                    schema = @Schema(
                            implementation = Sort.Direction.class,
                            enumAsRef = true
                    )
            )
            @ValidSortDirection
            String sortDirection
    ) {
        Sort sort;
        if (sortField != null) {
            sort = Sort.by(new Sort.Order(
                    Sort.Direction.fromString(sortDirection),
                    Asset.SortField.fromString(sortField).getFieldName()
            ));
        } else {
            sort = Sort.by(Collections.emptyList());
        }
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        return readPage(pageable);
    }

    @Secured("ROLE_write:asset")
    @RequestMapping(
            path = "/{id}",
            method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Updates an existing Asset")
    @SecurityRequirement(
            name = "OIDC",
            scopes = "write:access"
    )
    @ApiResponse(
            responseCode = "200",
            description = "The requested asset was updated successfully",
            content = @Content(
                    schema = @Schema(implementation = Asset.class)
            )
    )
    @GenericApiErrorResponses
    public ResponseEntity<?> updateAsset(
            @PathVariable("id")
            @Schema(
                    description = "The ID of the Asset to be updated",
                    minLength = 36,
                    maxLength = 36,
                    example = ValidationUtils.UUID_EXAMPLE_OBJECT
            )
            @Pattern(
                    regexp = ValidationUtils.UUID_PATTERN,
                    flags = Pattern.Flag.CASE_INSENSITIVE,
                    message = "must be a valid UUID"
            )
            String id,
            @RequestBody
            @NotNull(message = "A request body must be provided")
            @Valid
            AssetDto asset
    ) {
        return update(parseIdString(id), asset);
    }

    @Secured("ROLE_write:asset")
    @RequestMapping(
            path = "/{id}",
            method = RequestMethod.DELETE
    )
    @Operation(summary = "Deletes an Asset")
    @SecurityRequirement(
            name = "OIDC",
            scopes = "write:access"
    )
    @ApiResponse(
            responseCode = "204",
            description = "The requested Asset was deleted successfully"
    )
    @GenericApiErrorResponses
    public ResponseEntity<?> deleteAsset(
            @PathVariable("id")
            @Schema(
                    description = "The ID of the Asset to be deleted",
                    minLength = 36,
                    maxLength = 36,
                    example = ValidationUtils.UUID_EXAMPLE_OBJECT
            )
            @Pattern(
                    regexp = ValidationUtils.UUID_PATTERN,
                    flags = Pattern.Flag.CASE_INSENSITIVE,
                    message = "must be a valid UUID"
            )
            String id
    ) {
        return delete(parseIdString(id));
    }
}
