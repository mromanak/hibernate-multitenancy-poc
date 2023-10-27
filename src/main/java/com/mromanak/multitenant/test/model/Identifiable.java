package com.mromanak.multitenant.test.model;

/**
 * A marker class used to associate an entity class with the class of its identifier
 *
 * @param <I> The identifier type for the entity class
 */
public interface Identifiable<I> {

    I getId();

    void setId(I id);
}
