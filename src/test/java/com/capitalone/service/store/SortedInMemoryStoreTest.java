package com.capitalone.service.store;

import com.capitalone.exception.EntityExistsException;
import com.capitalone.exception.EntityNotFoundException;
import com.capitalone.model.Measurement;
import com.capitalone.util.Constants;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit test for SortedInMemoryStore.
 */
public class SortedInMemoryStoreTest {
    private final Store<String, Measurement> store = SortedInMemoryStore.getInstance();

    @Before
    public void resetStore() {
        store.clear();
    }

    @Test
    public void createValidMeasurementIsPersisted() throws Exception {
        final String entityId = "2015-09-01T16:00:00.000Z";
        final Measurement m = new Measurement(entityId, ImmutableMap.of(Constants.TEMPERATURE, 35.2F));
        store.create(m, entityId);
        final Measurement retrieved = store.retrieve(entityId);
        assertEquals("New measurement is persisted", m, retrieved);
    }

    @Test(expected = EntityExistsException.class)
    public void createAlreadyExistingMeasurementThrowsException() throws Exception {
        final Map<String, Measurement> measurements = setupMeasurementsStore();
        final String entityId = ImmutableSortedSet.copyOf(measurements.keySet()).asList().get(4);
        final Measurement m = measurements.get(entityId);
        store.create(m, entityId);
    }

    @Test(expected = EntityNotFoundException.class)
    public void retrieveNonExistingMeasurementThrowsException() throws Exception {
        setupMeasurementsStore();
        store.retrieve("2015-09-01T18:00:00.000Z");
    }

    @Test
    public void retrieveExistingMeasurementIsReturned() throws Exception {
        final Map<String, Measurement> measurements = setupMeasurementsStore();
        final String entityId = ImmutableSortedSet.copyOf(measurements.keySet()).asList().get(1);
        final Measurement retrieved = store.retrieve(entityId);
        assertEquals("Measurement ID 2015-09-01T16:10:00.000Z can be retrieved", measurements.get(entityId), retrieved);

    }

    @Test
    public void retrieveInRangeExistingMeasurementsWithInclusiveRangeAreReturned() throws Exception {
        final Map<String, Measurement> measurements = setupMeasurementsStore();
        final String entityIdFrom = ImmutableSortedSet.copyOf(measurements.keySet()).asList().get(1);
        final String entityIdTo = ImmutableSortedSet.copyOf(measurements.keySet()).asList().get(3);
        final Collection<Measurement> retrieved = store.retrieveInRange(entityIdFrom, true, entityIdTo, true);
        assertEquals("Three measurements are returned", 3, retrieved.size());
        assertTrue("Retrieved contains Measurement ID: 2015-09-01T16:10:00.000Z", retrieved.contains(measurements.get("2015-09-01T16:10:00.000Z")));
        assertTrue("Retrieved contains Measurement ID: 2015-09-01T16:20:00.000Z", retrieved.contains(measurements.get("2015-09-01T16:20:00.000Z")));
        assertTrue("Retrieved contains Measurement ID: 2015-09-01T16:30:00.000Z", retrieved.contains(measurements.get("2015-09-01T16:30:00.000Z")));
    }

    @Test
    public void retrieveInRangeExistingMeasurementsWithExclusiveRangeAreReturned() throws Exception {
        final Map<String, Measurement> measurements = setupMeasurementsStore();
        final String entityIdFrom = ImmutableSortedSet.copyOf(measurements.keySet()).asList().get(1);
        final String entityIdTo = ImmutableSortedSet.copyOf(measurements.keySet()).asList().get(3);
        final Collection<Measurement> retrieved = store.retrieveInRange(entityIdFrom, false, entityIdTo, false);
        assertEquals("Three measurements are returned", 1, retrieved.size());
        assertTrue("Retrieved contains Measurement ID: 2015-09-01T16:20:00.000Z", retrieved.contains(measurements.get("2015-09-01T16:20:00.000Z")));
    }

    @Test
    public void updateExistingMeasurementWithNewMetricsArePersisted() throws Exception {
        final Map<String, Measurement> measurements = setupMeasurementsStore();
        final String entityId = ImmutableSortedSet.copyOf(measurements.keySet()).asList().get(3);
        final Measurement m = measurements.get(entityId);
        final Map<String, Float> updatedMetrics = ImmutableMap.of(Constants.TEMPERATURE, 100F, Constants.DEW_POINT, 50F, Constants.PRECIPITATION, 0F);
        final Measurement mUpdated = new Measurement(m.getTimestamp(), updatedMetrics);
        store.update(mUpdated, entityId);
        assertEquals("Measurement ID 2015-09-01T16:20:00.000Z has been updated.", mUpdated, store.retrieve(entityId));
    }

    @Test(expected = EntityNotFoundException.class)
    public void updateNonExistingMeasurementThrowsException() throws Exception {
        final String entityId = "2015-09-01T16:00:00.000Z";
        final Map<String, Float> updatedMetrics = ImmutableMap.of(Constants.TEMPERATURE, 100F, Constants.DEW_POINT, 50F, Constants.PRECIPITATION, 0F);
        final Measurement mUpdated = new Measurement("2015-09-01T16:00:00.000Z", updatedMetrics);
        store.update(mUpdated, entityId);
    }

    @Test(expected = EntityNotFoundException.class)
    public void deleteExistingMeasurementRemovesMeasurement() throws Exception {
        final Map<String, Measurement> measurements = setupMeasurementsStore();
        final String entityId = ImmutableSortedSet.copyOf(measurements.keySet()).asList().get(7);
        assertNotNull("Measurement ID: 2015-09-01T17:10:00.000Z exists.", store.retrieve(entityId));
        store.delete(entityId);
        //This call should throw the exception
        store.retrieve(entityId);
    }

    @Test(expected = EntityNotFoundException.class)
    public void deleteNonExistingMeasurementThrowsException() throws Exception {
        store.delete("2015-09-01T16:30:00.000Z");
    }

    private Map<String, Measurement> setupMeasurementsStore() {
        final Map<String, Measurement> measurements = Maps.newHashMap();
        measurements.put("2015-09-01T16:00:00.000Z",
                new Measurement("2015-09-01T16:00:00.000Z", ImmutableMap.of(Constants.TEMPERATURE, 29.2F, Constants.DEW_POINT, 12F, Constants.PRECIPITATION, 10F)));
        measurements.put("2015-09-01T16:10:00.000Z",
                new Measurement("2015-09-01T16:10:00.000Z", ImmutableMap.of(Constants.TEMPERATURE, 31.2F, Constants.DEW_POINT, 12F, Constants.PRECIPITATION, 10F)));
        measurements.put("2015-09-01T16:20:00.000Z",
                new Measurement("2015-09-01T16:20:00.000Z", ImmutableMap.of(Constants.TEMPERATURE, 32.2F, Constants.DEW_POINT, 8F, Constants.PRECIPITATION, 10F)));
        measurements.put("2015-09-01T16:30:00.000Z",
                new Measurement("2015-09-01T16:30:00.000Z", ImmutableMap.of(Constants.TEMPERATURE, 33.2F, Constants.DEW_POINT, 11F, Constants.PRECIPITATION, 10F)));
        measurements.put("2015-09-01T16:40:00.000Z",
                new Measurement("2015-09-01T16:40:00.000Z", ImmutableMap.of(Constants.TEMPERATURE, 35.2F, Constants.DEW_POINT, 8F, Constants.PRECIPITATION, 9F)));
        measurements.put("2015-09-01T16:50:00.000Z",
                new Measurement("2015-09-01T16:50:00.000Z", ImmutableMap.of(Constants.TEMPERATURE, 35.2F, Constants.DEW_POINT, 8F, Constants.PRECIPITATION, 10F)));
        measurements.put("2015-09-01T17:00:00.000Z",
                new Measurement("2015-09-01T17:00:00.000Z", ImmutableMap.of(Constants.TEMPERATURE, 18F, Constants.DEW_POINT, 8F, Constants.PRECIPITATION, 10F)));
        measurements.put("2015-09-01T17:10:00.000Z",
                new Measurement("2015-09-01T17:10:00.000Z", ImmutableMap.of(Constants.TEMPERATURE, 24.2F, Constants.DEW_POINT, 8F, Constants.PRECIPITATION, 8F)));
        measurements.put("2015-09-01T17:20:00.000Z",
                new Measurement("2015-09-01T17:20:00.000Z", ImmutableMap.of(Constants.TEMPERATURE, 28F, Constants.DEW_POINT, 8F, Constants.PRECIPITATION, 10F)));
        measurements.put("2015-09-01T17:30:00.000Z",
                new Measurement("2015-09-01T17:30:00.000Z", ImmutableMap.of(Constants.TEMPERATURE, 35.2F, Constants.DEW_POINT, 8F, Constants.PRECIPITATION, 8F)));
        for (Map.Entry<String, Measurement> entry : measurements.entrySet())
            store.create(entry.getValue(), entry.getKey());

        return ImmutableMap.copyOf(measurements);
    }
}