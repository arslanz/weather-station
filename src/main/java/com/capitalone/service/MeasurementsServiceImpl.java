package com.capitalone.service;

import com.capitalone.exception.EntityNotFoundException;
import com.capitalone.model.Measurement;
import com.capitalone.service.store.SortedInMemoryStore;
import com.capitalone.service.store.Store;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Map;

/**
 * A service level class that implements all of the operations that can be performed on a Measurement.
 */
public class MeasurementsServiceImpl implements MeasurementsService {
    private final Store<String, Measurement> measurementStore;

    public MeasurementsServiceImpl() {
        this(SortedInMemoryStore.getInstance());
    }

    /**
     * Inject another implementation of the Store interface.
     * @param store the store to use must use Long and store Measurement.
     */
    MeasurementsServiceImpl(final Store<String, Measurement> store) {
        this.measurementStore = store;
    }

    @Override
    public void create(final Measurement m) {
        measurementStore.create(m, m.getTimestamp());
    }

    @Override
    public Measurement retrieve(final String timestamp) {
        return measurementStore.retrieve(timestamp);
    }

    @Override
    public Collection<Measurement> retrieveAll(final String date) {
        final String fromTimestamp = date + "T00:00:00.000Z";
        final String toTimestamp = date + "T23:59:59.999Z";
        return measurementStore.retrieveInRange(fromTimestamp, true, toTimestamp, true);
    }

    @Override
    public Collection<Measurement> retrieveAll(final String fromTimestamp, final String toTimestamp) {
        return measurementStore.retrieveInRange(fromTimestamp, true, toTimestamp, false);
    }

    @Override
    public Measurement updateWhole(final Measurement m) {
        return measurementStore.update(m, m.getTimestamp());
    }

    @Override
    public Measurement updatePartial(final Measurement m) {
        final Measurement existingM = measurementStore.retrieve(m.getTimestamp());
        if (existingM == null)
            throw new EntityNotFoundException(String.format("Measurement with id %s not found.", m.getTimestamp()));
        final Measurement updatedM = applyPartialUpdate(existingM, m);

        return measurementStore.update(updatedM, m.getTimestamp());
    }

    @Override
    public Measurement delete(final String timestamp) {
        return measurementStore.delete(timestamp);
    }

    /**
     * Clears the contents of the measurement store.
     * This can be used during testing to reset the state of the store.
     */
    protected void clearAll() {
        measurementStore.clear();
    }

    /**
     * Updates an existing Measurement with an a delta update.
     * @param existing the existing Measurement
     * @param deltaUpdate the delta update
     * @return the updated Measurement
     */
    private Measurement applyPartialUpdate(final Measurement existing, final Measurement deltaUpdate) {
        final Map<String, Float> updatedMetrics = Maps.newHashMap();
        updatedMetrics.putAll(existing.getMetrics());
        updatedMetrics.putAll(deltaUpdate.getMetrics());

        return new Measurement(existing.getTimestamp(), updatedMetrics);
    }

}
