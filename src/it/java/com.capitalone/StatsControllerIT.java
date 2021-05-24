package com.capitalone;

import com.capitalone.controller.MeasurementsController;
import com.capitalone.controller.StatsController;
import com.capitalone.model.Measurement;
import com.capitalone.model.Stat;
import com.capitalone.util.Constants;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Integration test class covering the test cases for endpoint 'stats'.
 */
public class StatsControllerIT extends JerseyTest {
    private static final String STAT = "stat";
    private static final String METRIC = "metric";
    private static final String FROM_DATE_TIME = "fromDateTime";
    private static final String TO_DATE_TIME = "toDateTime";

    /**
     * Jersey test configuration setup
     */
    @Override
    protected void configureClient(final ClientConfig config) {
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(MeasurementsController.class, StatsController.class);
    }

    @Test
    public void getStats_wellReportedMetric() {
        setupMeasurements();
        final Response response = target("stats")
                .queryParam(STAT, "min")
                .queryParam(STAT, "max")
                .queryParam(STAT, "average")
                .queryParam(METRIC, Constants.TEMPERATURE)
                .queryParam(FROM_DATE_TIME, "2015-09-01T16:00:00.000Z")
                .queryParam(TO_DATE_TIME, "2015-09-01T17:00:00.000Z")
                .request().get();
        final Collection<Stat> stats = response.readEntity(new GenericType<List<Stat>>(){});
        assertEquals("Status is 200", 200, response.getStatus());
        assertEquals("Three stat results", 3, stats.size());
        final Map<String, Stat> actualStats = Maps.uniqueIndex(stats, s -> s.getMetric() + ":" + s.getStat());
        verifyExpectedStats(actualStats, ImmutableMap.of("temperature:min", 27.1f, "temperature:max", 27.5f, "temperature:average", 27.3f));
    }

    @Test
    public void getStats_sparselyReportedMetric() {
        setupMeasurements();
        final Response response = target("stats")
                .queryParam(STAT, "min")
                .queryParam(STAT, "max")
                .queryParam(STAT, "average")
                .queryParam(METRIC, Constants.DEW_POINT)
                .queryParam(FROM_DATE_TIME, "2015-09-01T16:00:00.000Z")
                .queryParam(TO_DATE_TIME, "2015-09-01T17:00:00.000Z")
                .request().get();
        final Collection<Stat> stats = response.readEntity(new GenericType<List<Stat>>(){});
        assertEquals("Status is 200", 200, response.getStatus());
        assertEquals("Three stat results", 3, stats.size());
        final Map<String, Stat> actualStats = Maps.uniqueIndex(stats, s -> s.getMetric() + ":" + s.getStat());
        verifyExpectedStats(actualStats, ImmutableMap.of("dewPoint:min", 16.9f, "dewPoint:max", 17.3f, "dewPoint:average", 17.1f));
    }

    @Test
    public void getStats_unreportedMetric() {
        setupMeasurements();
        final Response response = target("stats")
                .queryParam(STAT, "min")
                .queryParam(STAT, "max")
                .queryParam(STAT, "average")
                .queryParam(METRIC, Constants.PRECIPITATION)
                .queryParam(FROM_DATE_TIME, "2015-09-01T16:00:00.000Z")
                .queryParam(TO_DATE_TIME, "2015-09-01T17:00:00.000Z")
                .request().get();
        final Collection<Stat> stats = response.readEntity(new GenericType<List<Stat>>(){});
        assertEquals("Status is 200", 200, response.getStatus());
        assertEquals("Three stat results", 0, stats.size());
    }

    @Test
    public void getStats_multipleMetrics() {
        setupMeasurements();
        final Response response = target("stats")
                .queryParam(STAT, "min")
                .queryParam(STAT, "max")
                .queryParam(STAT, "average")
                .queryParam(METRIC, Constants.TEMPERATURE)
                .queryParam(METRIC, Constants.DEW_POINT)
                .queryParam(METRIC, Constants.PRECIPITATION)
                .queryParam(FROM_DATE_TIME, "2015-09-01T16:00:00.000Z")
                .queryParam(TO_DATE_TIME, "2015-09-01T17:00:00.000Z")
                .request().get();
        final Collection<Stat> stats = response.readEntity(new GenericType<List<Stat>>(){});
        assertEquals("Status is 200", 200, response.getStatus());
        assertEquals("Three stat results", 6, stats.size());
        final Map<String, Stat> actualStats = Maps.uniqueIndex(stats, s -> s.getMetric() + ":" + s.getStat());
        final Map<String, Float> expectedValues = ImmutableMap.<String, Float>builder()
                .put("temperature:min", 27.1f).put("temperature:max", 27.5f).put("temperature:average", 27.3f)
                .put("dewPoint:min", 16.9f).put("dewPoint:max", 17.3f).put("dewPoint:average", 17.1f)
                .build();
        verifyExpectedStats(actualStats, expectedValues);
    }

    /**
     * Helper methods.
     */
    private void verifyExpectedStats(final Map<String, Stat> actualStats, final Map<String, Float> expectedValues) {
        expectedValues.entrySet().forEach(e -> {
            final String key = e.getKey();
            final float actualValue = actualStats.get(key).getValue();
            final float expectedValue = e.getValue();
            assertEquals(String.format("%s should be %f", key, expectedValue), expectedValue, actualValue, 0f);
        });
    }

    private Collection<Measurement> setupMeasurements() {
        final ImmutableList.Builder<Measurement> builder = ImmutableList.builder();
        builder.add(new Measurement("2015-09-01T16:00:00.000Z", createMetrics(27.1f, 16.9f)));
        builder.add(new Measurement("2015-09-01T16:10:00.000Z", createMetrics(27.3f)));
        builder.add(new Measurement("2015-09-01T16:20:00.000Z", createMetrics(27.5f, 17.1f)));
        builder.add(new Measurement("2015-09-01T16:30:00.000Z", createMetrics(27.4f, 17.3f)));
        builder.add(new Measurement("2015-09-01T16:40:00.000Z", createMetrics(27.2f)));
        builder.add(new Measurement("2015-09-01T17:00:00.000Z", createMetrics(28.1f, 18.3f)));
        final List<Measurement> measurements = builder.build();
        measurements.forEach(m -> target("measurements").request().post(Entity.json(m)));
        return measurements;
    }

    private static Map<String, Float> createMetrics(final Float temperature) {
        return ImmutableMap.of(Constants.TEMPERATURE, temperature);
    }

    private static Map<String, Float> createMetrics(final Float temperature, final Float dewPoint) {
        return ImmutableMap.of(Constants.TEMPERATURE, temperature, Constants.DEW_POINT, dewPoint);
    }
}
