package com.capitalone.service;

import com.capitalone.model.Stat;

/**
 * A service level interface that defines the supported statistic operations for a measurement.
 */
public interface StatsService {
    /**
     * Gets the average for the metric within the bounds of {@code fromDateTime} and {@code toDateTime}
     * @param metric the metric to use for the average
     * @param fromDateTime the date-time to retrieve the metric data FROM
     * @param toDateTime the date-time to retrieve the metric data UNTIL
     * @return the calculated average as a Stat
     */
    Stat getAverage(final String metric, final String fromDateTime, final String toDateTime);

    /**
     * Gets the maximum for the metric within the bounds of {@code fromDateTime} and {@code toDateTime}
     * @param metric the metric to scan for the maximum
     * @param fromDateTime the date-time to retrieve the metric data FROM
     * @param toDateTime the date-time to retrieve the metric data UNTIL
     * @return the calculated maximum as a Stat
     */
    Stat getMax(final String metric, final String fromDateTime, final String toDateTime);


    /**
     * Gets the minimum for the metric within the bounds of {@code fromDateTime} and {@code toDateTime}
     * @param metric the metric to scan for the minimum
     * @param fromDateTime the date-time to retrieve the metric data FROM
     * @param toDateTime the date-time to retrieve the metric data UNTIL
     * @return the calculated minimum as a Stat
     */
    Stat getMin(final String metric, final String fromDateTime, final String toDateTime);
}
