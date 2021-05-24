package com.capitalone.service.store;

import com.capitalone.exception.AppServerException;

import java.util.Collection;

/**
 * An interface specifying the methods that must be implemented to manage storing of an entity.
 */
public interface Store<I, E> {
    /**
     * Creates a new entity type E in the store using the entity ID of type I.
     * @param entity the entity to persist
     * @param entityId the unique entity ID to persist
     * @throws AppServerException an exception resulting from this application
     */
    void create(E entity, I entityId) throws AppServerException;

    /**
     * Retrieves an entity using entity ID.
     * @param entityId the unique ID for the entity to retrieve
     * @return the entity matching the unique ID
     * @throws AppServerException an exception resulting from this application
     */
    E retrieve(I entityId) throws AppServerException;

    /**
     * Retrieves a collection of entities within an ID range.
     * @param fromEntityId the "from" entity ID boundary
     * @param fromInclusive flag to indicate whether the "from" ID should be included for matching
     * @param toEntityId the "to" entity ID boundary
     * @param toInclusive flag to indicate whether the "to" ID should be included for matching
     * @return the collection of entities within the range specified
     * @throws AppServerException an exception resulting from this application
     */
    Collection<E> retrieveInRange(I fromEntityId, boolean fromInclusive, I toEntityId, boolean toInclusive) throws AppServerException;

    /**
     * Updates the entity matching the ID by replacing with the entity specified.
     * @param entity the updated entity to persist
     * @param entityId the unique entity ID to update
     * @return the updated entity
     * @throws AppServerException an exception resulting from this application
     */
    E update(E entity, I entityId) throws AppServerException;

    /**
     * Deletes the entity matching the ID specified.
     * @param entityId the unique entity ID to delete
     * @return the deleted entity
     * @throws AppServerException an exception resulting from this application
     */
    E delete(I entityId) throws AppServerException;

    /**
     * Clears the contents of the store entirely. Use with caution.
     * @throws AppServerException an exception resulting from this application
     */
    void clear() throws AppServerException;
}
