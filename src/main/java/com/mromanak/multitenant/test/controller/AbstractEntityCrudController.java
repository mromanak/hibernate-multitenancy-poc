package com.mromanak.multitenant.test.controller;

import com.mromanak.multitenant.test.model.DtoFor;
import com.mromanak.multitenant.test.model.Identifiable;
import com.mromanak.multitenant.test.model.error.ErrorResponse;
import com.mromanak.multitenant.test.service.EntityCrudService;
import org.slf4j.Logger;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

/**
 * Implements the basic skeleton of a controller that performs CRUD operations for a given entity class
 *
 * @param <I> The identifier type for the entity class
 * @param <D> The DTO type for the entity class
 * @param <E> The entity class
 */
public abstract class AbstractEntityCrudController<I, D extends DtoFor<I, E>, E extends Identifiable<I>> implements EntityCrudController<I, D, E> {

    private final EntityCrudService<I, D, E> service;

    protected AbstractEntityCrudController(EntityCrudService<I, D, E> service) {
        this.service = service;
    }

    protected abstract Logger getLogger();

    protected abstract String getEntityClassName();

    protected abstract I parseIdString(String idString);

    protected <T> ResponseEntity<T> successResponse(T responseBody) {
        return ResponseEntity.ok(responseBody);
    }

    protected ResponseEntity<ErrorResponse> notFoundResponse(I id) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorMessage(String.format("No %s entity found with ID %s", getEntityClassName(), id));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    protected ResponseEntity<ErrorResponse> internalErrorResponse(String message) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorMessage(message);

        return ResponseEntity.internalServerError().body(errorResponse);
    }

    @Override
    public ResponseEntity<?> create(D dto) {
        try {
            return successResponse(service.create(dto));
        } catch (Exception e) {
            getLogger().error("An error occurred while creating a(n) {} entity: {}", getEntityClassName(), e.getMessage(), e);
            return internalErrorResponse(
                    String.format("An internal error occurred while creating a(n) %s entity", getEntityClassName())
            );
        }
    }

    @Override
    public ResponseEntity<?> read(I id) {
        try {
            Optional<E> entityOpt = service.read(id);
            if (entityOpt.isPresent()) {
                return successResponse(entityOpt.get());
            } else {
                return notFoundResponse(id);
            }
        } catch (Exception e) {
            getLogger().error("An error occurred while getting a(n) {} entity: {}", getEntityClassName(), e.getMessage(), e);
            return internalErrorResponse(
                    String.format("An internal error occurred while getting a(n) %s entity", getEntityClassName())
            );
        }
    }

    @Override
    public ResponseEntity<?> readMultiple(Iterable<I> ids) {
        try {
            return successResponse(service.readMultiple(ids));
        } catch (Exception e) {
            getLogger().error("An error occurred while getting multiple {} entities: {}", getEntityClassName(), e.getMessage(), e);
            return internalErrorResponse(
                    String.format("An internal error occurred while getting multiple %s entities", getEntityClassName())
            );
        }
    }

    @Override
    public ResponseEntity<?> readPage(Pageable pageRequest) {
        try {
            return successResponse(service.readPage(pageRequest));
        } catch (Exception e) {
            getLogger().error("An error occurred while getting a page of {} entities ({}): {}", getEntityClassName(), pageRequest, e.getMessage(), e);
            return internalErrorResponse(
                    String.format("An internal error occurred while getting a page of %s entities (%s)", getEntityClassName(), pageRequest)
            );
        }
    }

    @Override
    public ResponseEntity<?> update(I id, D dto) {
        try {
            Optional<E> entityOpt = service.update(id, dto);
            if (entityOpt.isPresent()) {
                return successResponse(entityOpt.get());
            } else {
                return notFoundResponse(id);
            }
        } catch (Exception e) {
            getLogger().error("An error occurred while updating a(n) {} entity: {}", getEntityClassName(), e.getMessage(), e);
            return internalErrorResponse(
                    String.format("An internal error occurred while updating a(n) %s entity", getEntityClassName())
            );
        }
    }

    @Override
    public ResponseEntity<?> delete(I id) {
        try {
            if (service.delete(id)) {
                return ResponseEntity.noContent().build();
            }
            return notFoundResponse(id);
        } catch (Exception e) {
            getLogger().error("An error occurred while deleting a(n) {} entity: {}", getEntityClassName(), e.getMessage(), e);
            return internalErrorResponse(
                    String.format("An internal error occurred while deleting a(n) %s entity", getEntityClassName())
            );
        }
    }
}
