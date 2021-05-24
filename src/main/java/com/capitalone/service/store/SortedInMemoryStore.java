package com.capitalone.service.store;

import com.capitalone.exception.AppServerException;
import com.capitalone.exception.EntityExistsException;
import com.capitalone.exception.EntityNotFoundException;

import java.util.Collection;
import java.util.Collections;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * A simple in-memory store for generic entities.
 * The generic type parameter I represents the entity ID
 * The generic type parameter E represents the entity to be stored.
 *
 * This class is singleton to represent one single state of storage i.e. like a diskspace or a database there is one
 * instance where the data is stored. In an ideal case, this whole class would be replaced with a database DAO and the
 * singleton pattern can be removed because the database itself would act as the global state.
 *
 * Internally, a TreeMap is used to store the data because it allows a sub-map to be returned with from/to parameters.
 * This allows the store to be queried for a multiple entities using an entity ID range.
 */
public final class SortedInMemoryStore<I, E> implements Store<I, E> {
    private final NavigableMap<I, E> inMemoryStore = Collections.synchronizedNavigableMap(new TreeMap<>());
    private static Store instance;

    //Private constructor to prevent more than one instance being created.
    private SortedInMemoryStore(){}

    /**
     * Returns an instance of the store.
     * @return an instance of the store.
     */
    public synchronized static <I, E> Store<I, E> getInstance() {
        if (instance == null)
            instance = new SortedInMemoryStore<I, E>();
        return (Store<I, E>) instance;
    }

    @Override
    public void create(final E entity, final I entityId) throws AppServerException {
        if (inMemoryStore.containsKey(entityId))
            throw new EntityExistsException("Cannot create new entity. Entity already exists with ID: " + entityId);

        inMemoryStore.put(entityId, entity);
    }

    @Override
    public E retrieve(final I entityId) throws AppServerException {
        if (!inMemoryStore.containsKey(entityId))
            throw new EntityNotFoundException("Cannot retrieve entity. Entity not found with ID " + entityId);

        return inMemoryStore.get(entityId);
    }

    @Override
    public Collection<E> retrieveInRange(
           final I fromEntityId,
            final boolean fromInclusive,
           final I toEntityId,
            final boolean toInclusive
    ) throws AppServerException {
        return inMemoryStore.subMap(fromEntityId, fromInclusive, toEntityId, toInclusive).values();
    }

    @Override
    public E update(final E entity,final I entityId) throws AppServerException {
        if (!inMemoryStore.containsKey(entityId))
            throw new EntityNotFoundException("Cannot update entity. Entity not found with ID " + entityId);

        return inMemoryStore.put(entityId, entity);
    }

    @Override
    public E delete(final I entityId) throws AppServerException {
        if (!inMemoryStore.containsKey(entityId))
            throw new EntityNotFoundException("Cannot delete entity. Entity not found with ID " + entityId);

        return inMemoryStore.remove(entityId);
    }

    @Override
    public void clear() throws AppServerException {
        inMemoryStore.clear();
    }
}
