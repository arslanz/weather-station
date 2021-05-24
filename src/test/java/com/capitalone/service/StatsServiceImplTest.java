package com.capitalone.service;

import com.capitalone.model.Measurement;
import com.capitalone.util.Constants;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the StatsServiceImpl class.
 */
public class StatsServiceImplTest {
    private final MeasurementsService measurementsServiceMock = Mockito.mock(MeasurementsService.class);
    private final StatsService statsService = new StatsServiceImpl(measurementsServiceMock);

    @Test
    public void averageCalculation() {
        setupMeasurementsServiceMock();
        final String from = "2015-09-01T16:00:00.000Z";
        final String to = "2015-09-01T16:30:00.000Z";
        final Collection<Measurement> retrieved = measurementsServiceMock.retrieveAll(from, to);
        float avgExpected = 0;
        for (final Measurement m : retrieved)
            avgExpected += m.getMetrics().get(Constants.TEMPERATURE);
        avgExpected = avgExpected / retrieved.size();
        final float avgActual = statsService.getAverage(Constants.TEMPERATURE, from, to).getValue();
        assertEquals("Average calculation is correct", avgExpected, avgActual, 0.000002F);
    }

    @Test
    public void maxCalculation() {
        setupMeasurementsServiceMock();
        final String from = "2015-09-01T16:00:00.000Z";
        final String to = "2015-09-01T17:00:00.000Z";
        final Collection<Measurement> retrieved = measurementsServiceMock.retrieveAll(from, to);
        float maxExpected = Float.MIN_VALUE;
        for (final Measurement m : retrieved) {
            float value = m.getMetrics().get(Constants.DEW_POINT);
            maxExpected = value > maxExpected ? value : maxExpected;
        }
        final float maxActual = statsService.getMax(Constants.DEW_POINT, from, to).getValue();
        assertEquals("Max calculation is correct", maxExpected, maxActual, 0F);
    }

    @Test
    public void minCalculation() {
        setupMeasurementsServiceMock();
        final String from = "2015-09-01T16:00:00.000Z";
        final String to = "2015-09-01T17:00:00.000Z";
        final Collection<Measurement> retrieved = measurementsServiceMock.retrieveAll(from, to);
        float minExpected = Float.MAX_VALUE;
        for (final Measurement m : retrieved) {
            float value = m.getMetrics().get(Constants.PRECIPITATION);
            minExpected = value < minExpected ? value : minExpected;
        }
        final float maxActual = statsService.getMin(Constants.PRECIPITATION, from, to).getValue();
        assertEquals("Min calculation is correct", minExpected, maxActual, 0F);
    }


    private void setupMeasurementsServiceMock() {
        final TreeMap<String, Measurement> measurements = Maps.newTreeMap();
        measurements.putAll(setupMeasurements());
        Mockito.when(measurementsServiceMock.retrieveAll(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(invocationOnMock -> {
                    final String from = invocationOnMock.getArgument(0);
                    final String to = invocationOnMock.getArgument(1);
                    return measurements.subMap(from, true, to, false).values();
                });
    }

    private Map<String, Measurement> setupMeasurements() {
        final Map<String, Measurement> measurements = Maps.newHashMap();
        measurements.put("2015-09-01T16:00:00.000Z",
                new Measurement("2015-09-01T16:00:00.000Z", ImmutableMap.of(Constants.TEMPERATURE, 29.2F, Constants.DEW_POINT, 12F, Constants.PRECIPITATION, 11F)));
        measurements.put("2015-09-01T16:10:00.000Z",
                new Measurement("2015-09-01T16:10:00.000Z", ImmutableMap.of(Constants.TEMPERATURE, 31.2F, Constants.DEW_POINT, 12F, Constants.PRECIPITATION, 1F)));
        measurements.put("2015-09-01T16:20:00.000Z",
                new Measurement("2015-09-01T16:20:00.000Z", ImmutableMap.of(Constants.TEMPERATURE, 32.2F, Constants.DEW_POINT, 8F, Constants.PRECIPITATION, 5F)));
        measurements.put("2015-09-01T16:30:00.000Z",
                new Measurement("2015-09-01T16:30:00.000Z", ImmutableMap.of(Constants.TEMPERATURE, 33.2F, Constants.DEW_POINT, 11F, Constants.PRECIPITATION, 6F)));
        measurements.put("2015-09-01T16:40:00.000Z",
                new Measurement("2015-09-01T16:40:00.000Z", ImmutableMap.of(Constants.TEMPERATURE, 35.2F, Constants.DEW_POINT, 8F, Constants.PRECIPITATION, 9F)));
        measurements.put("2015-09-01T16:50:00.000Z",
                new Measurement("2015-09-01T16:50:00.000Z", ImmutableMap.of(Constants.TEMPERATURE, 35.2F, Constants.DEW_POINT, 8F, Constants.PRECIPITATION, 2F)));
        measurements.put("2015-09-01T17:00:00.000Z",
                new Measurement("2015-09-01T17:00:00.000Z", ImmutableMap.of(Constants.TEMPERATURE, 18F, Constants.DEW_POINT, 8F, Constants.PRECIPITATION, 4F)));
        measurements.put("2015-09-01T17:10:00.000Z",
                new Measurement("2015-09-01T17:10:00.000Z", ImmutableMap.of(Constants.TEMPERATURE, 24.2F, Constants.DEW_POINT, 8F, Constants.PRECIPITATION, 5F)));
        measurements.put("2015-09-01T17:20:00.000Z",
                new Measurement("2015-09-01T17:20:00.000Z", ImmutableMap.of(Constants.TEMPERATURE, 28F, Constants.DEW_POINT, 8F, Constants.PRECIPITATION, 7F)));
        measurements.put("2015-09-01T17:30:00.000Z",
                new Measurement("2015-09-01T17:30:00.000Z", ImmutableMap.of(Constants.TEMPERATURE, 35.2F, Constants.DEW_POINT, 8F, Constants.PRECIPITATION, 8F)));
        return ImmutableMap.copyOf(measurements);
    }
}