package com.capitalone.service;

import com.capitalone.model.Measurement;

import java.util.Collection;

/**
 * A service level interface that defines all of the operations that can be performed on a Measurement.
 */
public interface MeasurementsService {

    /**
     * Creates a new Measurement in the persistence layer.
     * @param m the Measurement to persist
     */
    void create(Measurement m);

    /**
     * Retrieves a Measurement by timestamp.
     * @param timestamp the timestamp of the Measurement
     * @return the retrieved Measurement
     */
    Measurement retrieve(String timestamp);

    /**
     * Retrieves all Measurement's from a specific date.
     * @param date the measurement date
     * @return the collection of Measurement's
     */
    Collection<Measurement> retrieveAll(String date);

    /**
     * Retrieves all Measurement's within a timestamp range.
     * @param fromTimestamp the 'from' timestamp
     * @param toTimestamp the 'to' timestamp
     * @return the collection of Measurement's
     */
    Collection<Measurement> retrieveAll(String fromTimestamp, String toTimestamp);

    /**
     * Updates an existing Measurement by taking the passed in Measurement as a "whole" update.
     * The existing Measurement is completely replaced by the new Measurement.
     * @param m the updated Measurement
     * @return the Measurement persisted
     */
    Measurement updateWhole(Measurement m);

    /**
     * Updates an existing Measurement by taking the passed in Measurement as a "partial" update.
     * Only the delta items specified in the update parameter are changed.
     * @param m the Measurement containing the delta updates
     * @return the Measurement persisted
     */
    Measurement updatePartial(Measurement m);

    /**
     * Deletes the Measurement at the specified timestamp.
     * @param timestamp the timestamp of the Measurement
     * @return the Measurement deleted
     */
    Measurement delete(String timestamp);
}
