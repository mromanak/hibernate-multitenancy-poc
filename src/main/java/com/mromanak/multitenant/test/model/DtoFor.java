package com.mromanak.multitenant.test.model;

/**
 * A marker interface used to associate a DTO class with an entity class and the class of the entity's identifier.
 *
 * @param <I> The identifier type for the entity class
 * @param <E> The entity class
 */
public interface DtoFor<I, E extends Identifiable<I>> {
}
