package com.mromanak.multitenant.test.controller;

import com.mromanak.multitenant.test.model.DtoFor;
import com.mromanak.multitenant.test.model.Identifiable;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

/**
 * Defines the contract of a controller that performs CRUD operations for a given entity class.
 * <p/>
 * TODO: This interface probably isn't necessary
 * TODO: If this interface is necessary, its name sucks
 *
 * @param <I> The identifier type for the entity class
 * @param <D> The DTO type for the entity class
 * @param <E> The entity class
 */
public interface EntityCrudController<I, D extends DtoFor<I, E>, E extends Identifiable<I>> {
    ResponseEntity<?> create(D dto);

    ResponseEntity<?> read(I id);

    ResponseEntity<?> readMultiple(Iterable<I> ids);

    ResponseEntity<?> readPage(Pageable pageRequest);

    ResponseEntity<?> update(I id, D dto);

    ResponseEntity<?> delete(I id);
}
