package com.mromanak.multitenant.test.service;

import com.mromanak.multitenant.test.model.DtoFor;
import com.mromanak.multitenant.test.model.Identifiable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Defines a contract for a service that performs basic CRUD operations for a given entity class.
 * <p/>
 * TODO: This name sucks
 *
 * @param <I> The identifier type for the entity class
 * @param <D> The DTO type for the entity class
 * @param <E> The entity class
 */
public interface EntityCrudService<I, D extends DtoFor<I, E>, E extends Identifiable<I>> {

    default E toEntity(D dto) {
        return toEntity(null, dto);
    }

    E toEntity(I id, D dto);

    D toDto(E entity);

    E create(D dto);

    Optional<E> read(I id);

    Iterable<E> readMultiple(Iterable<I> ids);

    Page<E> readPage(Pageable pageRequest);

    Optional<E> update(I id, D dto);

    boolean delete(I id);
}
