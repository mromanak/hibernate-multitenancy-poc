package com.mromanak.multitenant.test.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mromanak.multitenant.test.model.DtoFor;
import com.mromanak.multitenant.test.model.Identifiable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Objects;
import java.util.Optional;

/**
 * Implements the basic skeleton of a service that performs CRUD operations for a given entity class
 *
 * @param <I> The identifier type for the entity class
 * @param <D> The DTO type for the entity class
 * @param <E> The entity class
 */
public abstract class AbstractEntityCrudService<I, D extends DtoFor<I, E>, E extends Identifiable<I>>
        implements EntityCrudService<I, D, E> {

    protected final ObjectMapper objectMapper;
    protected final PagingAndSortingRepository<E, I> repository;
    protected final Class<? extends D> dtoClass;
    protected final Class<? extends E> entityClass;

    protected AbstractEntityCrudService(
            ObjectMapper objectMapper,
            PagingAndSortingRepository<E, I> repository,
            Class<? extends D> dtoClass,
            Class<? extends E> entityClass
    ) {
        Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        Objects.requireNonNull(repository, "repository must not be null");
        Objects.requireNonNull(dtoClass, "dtoClass must not be null");
        Objects.requireNonNull(entityClass, "entityClass must not be null");

        this.objectMapper = objectMapper;
        this.repository = repository;
        this.dtoClass = dtoClass;
        this.entityClass = entityClass;
    }

    /**
     * Applies the updates represented by a DTO object to an entity object
     *
     * @param updateDto A DTO containing the desired state of the entity
     * @param entity An entity containing the current state of the entity
     */
    protected abstract void applyUpdates(D updateDto, E entity);

    @Override
    public E toEntity(I id, D dto) {
        Objects.requireNonNull(dto, "dto must not be null");
        E entity = objectMapper.convertValue(dto, entityClass);
        entity.setId(id);
        return entity;
    }

    @Override
    public D toDto(E entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        return objectMapper.convertValue(entity, dtoClass);
    }

    @Override
    public E create(D dto) {
        Objects.requireNonNull(dto, "dto must not be null");
        return repository.save(toEntity(dto));
    }

    @Override
    public Optional<E> read(I id) {
        Objects.requireNonNull(id, "id must not be null");
        return repository.findById(id);
    }

    @Override
    public Iterable<E> readMultiple(Iterable<I> ids) {
        Objects.requireNonNull(ids, "ids must not be null");
        return repository.findAllById(ids);
    }

    @Override
    public Page<E> readPage(Pageable pageRequest) {
        Objects.requireNonNull(pageRequest, "pageRequest must not be null");
        return repository.findAll(pageRequest);
    }

    @Override
    public Optional<E> update(I id, D dto) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(dto, "dto must not be null");
        return repository.findById(id)
                .map((E entity) -> {
                    applyUpdates(dto, entity);
                    return repository.save(entity);
                });
    }

    @Override
    public boolean delete(I id) {
        Objects.requireNonNull(id, "id must not be null");
        var optEntity = repository.findById(id);
        if (optEntity.isPresent()) {
            repository.delete(optEntity.get());
            return true;
        }
        return false;
    }
}
