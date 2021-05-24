package com.capitalone.service;

import com.capitalone.model.Measurement;
import com.capitalone.model.Stat;
import com.capitalone.model.StatType;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * A service level class that implements the supported statistic operations for a measurement.
 */
@Slf4j
public class StatsServiceImpl implements StatsService {
    private final MeasurementsService measurementsService;

    public StatsServiceImpl() {
        this(new MeasurementsServiceImpl());
    }

    public StatsServiceImpl(final MeasurementsService measurementsService) {
        this.measurementsService = measurementsService;
    }

    @Override
    public Stat getAverage(final String metric, final String fromDateTime, final String toDateTime) {
        log.info("Getting average for metric {} from {} to {}", metric, fromDateTime, toDateTime);
        final Stat avgStat;
        final Collection<Float> metrics = collect(fromDateTime, toDateTime, metric);
        if (!metrics.isEmpty()) {
            final OptionalDouble calc = metrics.stream().mapToDouble(v -> v).average();
            avgStat = calc.isPresent() ? new Stat(metric, StatType.AVERAGE, (float)calc.getAsDouble()) : null;
        } else {
            log.info("Return null. No metrics for {} between {} and {}.", metric, fromDateTime, toDateTime);
            avgStat = null;
        }
        log.info("Done: Getting average for metric {} from {} to {}", metric, fromDateTime, toDateTime);
        return avgStat;
    }

    @Override
    public Stat getMax(final String metric, final String fromDateTime, final String toDateTime) {
        log.info("Getting max for metric {} from {} to {}", metric, fromDateTime, toDateTime);
        final Stat maxStat;
        final Collection<Float> metrics = collect(fromDateTime, toDateTime, metric);
        if (!metrics.isEmpty()) {
            final Optional<Float> calc = metrics.stream().max(Float::compare);
            maxStat = calc.isPresent() ? new Stat(metric, StatType.MAX, calc.get()) : null;
        } else {
            log.info("Return null. No metrics for {} between {} and {}.", metric, fromDateTime, toDateTime);
            maxStat = null;
        }
        log.info("Done: Getting max for metric {} from {} to {}", metric, fromDateTime, toDateTime);
        return maxStat;
    }

    @Override
    public Stat getMin(final String metric, final String fromDateTime, final String toDateTime) {
        log.info("Getting min for metric {} from {} to {}", metric, fromDateTime, toDateTime);
        final Stat minStat;
        final Collection<Float> metrics = collect(fromDateTime, toDateTime, metric);
        if (!metrics.isEmpty()) {
            final Optional<Float> calc = metrics.stream().min(Float::compare);
            minStat = calc.isPresent() ? new Stat(metric, StatType.MIN, calc.get()) : null;
        } else {
            log.info("Return null. No metrics for {} between {} and {}.", metric, fromDateTime, toDateTime);
            minStat = null;
        }
        log.info("Done: Getting min for metric {} from {} to {}", metric, fromDateTime, toDateTime);
        return minStat;
    }

    /**
     * Collects all metric values within the specified date-time range.
     * @param fromDateTime the date-time to retrieve the metric data FROM
     * @param toDateTime the date-time to retrieve the metric data UNTIL
     * @param metric the metric for which to collect values
     * @return the collection of metric values
     */
    private Collection<Float> collect(final String fromDateTime, final String toDateTime, final String metric) {
        final Collection<Measurement> measurements = measurementsService.retrieveAll(fromDateTime, toDateTime);
        final List<Float> values = Lists.newArrayList();
        for (final Measurement m : measurements) {
            final Float value = m.getMetrics().get(metric);
            if (value != null) values.add(value);
        }
        return values;
    }
}
