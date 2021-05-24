package com.capitalone;

import com.capitalone.controller.MeasurementsController;
import com.capitalone.model.Measurement;
import com.capitalone.service.MeasurementsServiceImpl;
import com.capitalone.util.Constants;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * Integration test class covering the test cases for endpoint 'measurements'.
 */
public class MeasurementsControllerIT extends JerseyTest {
    private static final String MEASUREMENT_KEY_METRICS = "metrics";
    private static final String MEASUREMENT_KEY_TIMESTAMP = "timestamp";
    private final ResettableMeasurementsService resettableMeasurementService = new ResettableMeasurementsService();

    /**
     * Jersey test configuration setup
     */
    @Override
    protected void configureClient(final ClientConfig config) {
        //Allows access to the PATCH controller method
        config.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(MeasurementsController.class);
    }

    @Before
    public void resetMeasurementStoreToEmpty() {
        resettableMeasurementService.resetToEmpty();
    }

    /**
     * Post measurement tests.
     */
    @Test
    public void postMeasurement_measurementWithValidValues() throws Exception {
        final String timestamp = "2015-09-01T16:00:00.000Z";
        final Map<String, Object> m = createMeasurementMap(timestamp, createObjectMetrics(27.1f, 16.7f, 0f));
        final Response response = target("measurements").request().post(Entity.json(m));

        final int expectedStatus = 201;
        final int actualStatus = response.getStatus();
        assertEquals("Status code should be 201", expectedStatus, actualStatus);

        final String expectedRedirect = "/measurements/" + timestamp;
        final String actualRedirect = response.getLocation().toString() == null ? "" : response.getLocation().toString().replaceFirst("http://localhost:\\d{4}", "");
        assertEquals("The redirect URI should end with the measurement endpoint and timestamp", expectedRedirect, actualRedirect);
    }

    @Test
    public void postMeasurement_measurementWithInvalidValues() {
        final Map<String, Object> m = createMeasurementMap(
                "2015-09-01T16:00:00.000Z",
                createObjectMetrics("Not a number", 16.7f, 0f));
        final Response response = target("measurements").request().post(Entity.json(m));
        //Response.Status.BAD_REQUEST == 400
        assertEquals("Status code should be 400", 400, response.getStatus());
        assertEquals("Reason should be 'Bad Request'", "Bad Request", response.getStatusInfo().getReasonPhrase());
    }

    @Test
    public void postMeasurement_measurementWithoutTimestamp() {
        final Map<String, Object> m = createMeasurementMap(createObjectMetrics(27.1f, 16.7f, 0f));
        final Response response = target("measurements").request().post(Entity.json(m));
        //Response.Status.BAD_REQUEST == 400
        assertEquals("Status code should be 400", 400, response.getStatus());
        assertEquals("Reason should be 'Bad Request'", "Bad Request", response.getStatusInfo().getReasonPhrase());
    }

    /**
     * Get measurement tests.
     */
    @Test
    public void getMeasurement_existingMeasurementTimestampReturnsCorrectStatusAndMeasurement() {
        setupMeasurements1();
        final String timestamp = "2015-09-01T16:20:00.000Z";
        final int expectedStatus = 200;
        final float expectedTemperature = 27.5f;
        final float expectedDewPoint = 17.1f;
        final float expectedPrecipitation = 0f;

        final Response response = target("measurements/" + timestamp).request().get();
        final Measurement m = response.readEntity(Measurement.class);
        assertEquals("Status is 200", expectedStatus, response.getStatus());
        assertEquals("Timestamp matches", timestamp, m.getTimestamp());
        assertEquals("Temperature matches", expectedTemperature, m.getMetrics().get(Constants.TEMPERATURE), 0f);
        assertEquals("DewPoint matches", expectedDewPoint, m.getMetrics().get(Constants.DEW_POINT), 0f);
        assertEquals("Precipitation matches", expectedPrecipitation, m.getMetrics().get(Constants.PRECIPITATION), 0f);
    }

    @Test
    public void getMeasurement_nonExistingTimestampReturnsStatus404() {
        setupMeasurements1();
        final Response response = target("measurements/2015-09-01T16:50:00.000Z").request().get();
        assertEquals("Status is 404", 404, response.getStatus());
    }

    @Test
    public void getMeasurement_existingDateReturnsCorrectStatusAllRelatedMeasurements() {
        final Collection<Measurement> measurements = setupMeasurements1();
        final String date = "2015-09-01";
        final Response response = target("measurements/" +  date).request().get();
        assertEquals("Status is 200", 200, response.getStatus());

        final Collection<Measurement> actual = response.readEntity(new GenericType<List<Measurement>>() {});
        final Collection<Measurement> expected = measurements.stream()
                .filter(m -> m.getTimestamp().startsWith(date))
                .collect(Collectors.toList());

        assertEquals("Resulting measurement count should be " + expected.size(), expected.size(), actual.size());
        final Map<String, Measurement> actualAsMap = Maps.uniqueIndex(actual, Measurement::getTimestamp);
        expected.forEach(e -> {
            final Map<String, Float> actualMetrics = actualAsMap.get(e.getTimestamp()).getMetrics();
            final Map<String, Float> expectedMetrics = e.getMetrics();
            assertEquals(e.getTimestamp()+" metrics size should match", expectedMetrics.size(), actualMetrics.size());
            assertEquals(e.getTimestamp()+" temperature should match", expectedMetrics.get(Constants.TEMPERATURE), actualMetrics.get(Constants.TEMPERATURE), 0f);
            assertEquals(e.getTimestamp()+" dewPoint should match", expectedMetrics.get(Constants.DEW_POINT), actualMetrics.get(Constants.DEW_POINT), 0f);
            assertEquals(e.getTimestamp()+" precipitation should match", expectedMetrics.get(Constants.PRECIPITATION), actualMetrics.get(Constants.PRECIPITATION), 0f);
        } );
    }

    @Test
    public void getMeasurement_nonExistingDateReturnsCorrectStatusAndNoMeasurements() {
        setupMeasurements1();
        final Response response = target("measurements/2015-09-03").request().get();
        assertEquals("Status is 404", 404, response.getStatus());
    }

    /**
     * Put measurement tests.
     */
    @Test
    public void putMeasurement_existingValidMeasurementReturnsCorrectStatusAndPreviousMeasurementCompletelyReplaced() {
        final Map<String, Measurement> measurements = Maps.uniqueIndex(setupMeasurements2(), Measurement::getTimestamp);
        final String timestamp = "2015-09-01T16:00:00.000Z";

        final Map<String, Float> beforeMetrics = measurements.get(timestamp).getMetrics();
        assertBeforeAfter("Before", "unmodified", timestamp, beforeMetrics);

        final float newTemp = beforeMetrics.get(Constants.TEMPERATURE);
        final float newDewPoint = beforeMetrics.get(Constants.DEW_POINT);
        final float newPrecipitation = 15.2f;
        final Measurement m = new Measurement(timestamp, createMetrics(newTemp, newDewPoint, newPrecipitation));
        final Response response = target("measurements/" + timestamp).request().put(Entity.json(m));
        assertEquals("Status is 204", 204, response.getStatus());
        assertBeforeAfter("After", "updated", timestamp, m.getMetrics());
    }

    @Test
    public void putMeasurement_existingInvalidMeasurementReturnsStatus400AndUnmodifiedMeasurement() {
        final Map<String, Measurement> measurements = Maps.uniqueIndex(setupMeasurements2(), Measurement::getTimestamp);
        final String timestamp = "2015-09-01T16:00:00.000Z";

        final Map<String, Float> beforeMetrics = measurements.get(timestamp).getMetrics();
        assertBeforeAfter("Before", "unmodified", timestamp, beforeMetrics);

        final String newTemp = "not a number";
        final float newDewPoint = beforeMetrics.get(Constants.DEW_POINT);
        final float newPrecipitation = 15.2f;
        final Map<String, Object> m = createMeasurementMap(
                timestamp,
                createObjectMetrics(newTemp, newDewPoint, newPrecipitation));
        final Response response = target("measurements/" + timestamp).request().put(Entity.json(m));
        //Response.Status.BAD_REQUEST == 400
        assertEquals("Status is 400", 400, response.getStatus());
        assertBeforeAfter("After", "unmodified", timestamp, beforeMetrics);
    }

    @Test
    public void putMeasurement_mismatchTimestampWithMeasurementReturnsStatus409AndUnmodifiedMeasurement() {
        final Map<String, Measurement> measurements = Maps.uniqueIndex(setupMeasurements2(), Measurement::getTimestamp);
        final String timestamp = "2015-09-01T16:00:00.000Z";

        final Map<String, Float> beforeMetrics = measurements.get(timestamp).getMetrics();
        assertBeforeAfter("Before", "unmodified", timestamp, beforeMetrics);

        final String mismatchTimestamp = "2015-09-02T16:00:00.000Z";
        final float newTemp = beforeMetrics.get(Constants.TEMPERATURE);
        final float newDewPoint = beforeMetrics.get(Constants.DEW_POINT);
        final float newPrecipitation = 15.2f;
        final Measurement m = new Measurement(mismatchTimestamp, createMetrics(newTemp, newDewPoint, newPrecipitation));
        final Response response = target("measurements/" + timestamp).request().put(Entity.json(m));
        assertEquals("Status is 409", 409, response.getStatus());
        assertBeforeAfter("After", "unmodified", timestamp, beforeMetrics);
    }

    @Test
    public void putMeasurement_nonExistingTimestampReturnsStatus404() {
        setupMeasurements2();
        final String timestamp = "2015-09-02T16:00:00.000Z";
        final float newTemp = 27.1f;
        final float newDewPoint = 16.7f;
        final float newPrecipitation = 15.2f;
        final Measurement m = new Measurement(timestamp, createMetrics(newTemp, newDewPoint, newPrecipitation));
        final Response response = target("measurements/" + timestamp).request().put(Entity.json(m));
        assertEquals("Status is 404", 404, response.getStatus());
    }

    /**
     * Patch measurement tests.
     */
    @Test
    public void patchMeasurement_existingValidMeasurementReturnsCorrectStatusAndPreviousMeasurementWithDeltaUpdate() {
        final Map<String, Measurement> measurements = Maps.uniqueIndex(setupMeasurements2(), Measurement::getTimestamp);
        final String timestamp = "2015-09-01T16:00:00.000Z";

        final Map<String, Float> beforeMetrics = measurements.get(timestamp).getMetrics();
        assertBeforeAfter("Before", "unmodified", timestamp, beforeMetrics);

        final float newPrecipitation = 12.3f;
        final Measurement m = new Measurement(timestamp, ImmutableMap.of(Constants.PRECIPITATION, newPrecipitation));
        final Response response = target("measurements/" + timestamp).request().method("PATCH", Entity.json(m));
        assertEquals("Status is 204", 204, response.getStatus());
        assertBeforeAfter("After", "updated", timestamp,
                createMetrics(beforeMetrics.get(Constants.TEMPERATURE), beforeMetrics.get(Constants.DEW_POINT), newPrecipitation));
    }

    @Test
    public void patchMeasurement_existingInvalidMeasurementReturnsStatus400AndUnmodifiedMeasurement() {
        final Map<String, Measurement> measurements = Maps.uniqueIndex(setupMeasurements2(), Measurement::getTimestamp);
        final String timestamp = "2015-09-01T16:00:00.000Z";

        final Map<String, Float> beforeMetrics = measurements.get(timestamp).getMetrics();
        assertBeforeAfter("Before", "unmodified", timestamp, beforeMetrics);

        final String newPrecipitation = "not a number";
        final Map<String, Object> m = createMeasurementMap(timestamp, ImmutableMap.of(Constants.PRECIPITATION, newPrecipitation));
        final Response response = target("measurements/" + timestamp).request().method("PATCH", Entity.json(m));
        //Response.Status.BAD_REQUEST == 400
        assertEquals("Status is 400", 400, response.getStatus());
        assertBeforeAfter("After", "updated", timestamp, beforeMetrics);
    }

    @Test
    public void patchMeasurement_mismatchTimestampWithMeasurementReturnsStatus409AndUnmodifiedMeasurement() {
        final Map<String, Measurement> measurements = Maps.uniqueIndex(setupMeasurements2(), Measurement::getTimestamp);
        final String timestamp = "2015-09-01T16:00:00.000Z";

        final Map<String, Float> beforeMetrics = measurements.get(timestamp).getMetrics();
        assertBeforeAfter("Before", "unmodified", timestamp, beforeMetrics);

        final String mismatchTimestamp = "2015-09-02T16:00:00.000Z";
        final float newPrecipitation = 12.3f;
        final Measurement m = new Measurement(mismatchTimestamp, ImmutableMap.of(Constants.PRECIPITATION, newPrecipitation));
        final Response response = target("measurements/" + timestamp).request().method("PATCH", Entity.json(m));
        assertEquals("Status is 409", 409, response.getStatus());
        assertBeforeAfter("After", "unmodified", timestamp, beforeMetrics);
    }

    @Test
    public void patchMeasurement_nonExistingTimestampReturnsStatus404() {
        setupMeasurements2();
        final String timestamp = "2015-09-02T16:00:00.000Z";
        final float newPrecipitation = 12.3f;
        final Measurement m = new Measurement(timestamp, ImmutableMap.of(Constants.PRECIPITATION, newPrecipitation));
        final Response response = target("measurements/" + timestamp).request().method("PATCH", Entity.json(m));
        assertEquals("Status is 404", 404, response.getStatus());
    }

    /**
     * Delete measurement tests.
     */
    @Test
    public void deleteMeasurement_existingMeasurementTimestampReturnsCorrectStatusAndIsActuallyRemoved() {
        setupMeasurements2();
        final String deleteTimestamp = "2015-09-01T16:00:00.000Z";
        final String unmodifiedTimestamp = "2015-09-01T16:10:00.000Z";

        final Response delete = target("measurements/" + deleteTimestamp).request().delete();
        assertEquals("Status is 204", 204, delete.getStatus());

        //Verify that calling the same request again leads to a NOT_FOUND 404 response
        final Response getDeleted = target("measurements/" + deleteTimestamp).request().get();
        assertEquals("Status is 404", 404, getDeleted.getStatus());

        //Verify other measurement still exists
        final Response getUnmodified = target("measurements/" + unmodifiedTimestamp).request().get();
        assertEquals("Status is 200", 200, getUnmodified.getStatus());
        assertNotNull("Non matching measurement still exists", getUnmodified.readEntity(Measurement.class));
    }

    @Test
    public void deleteMeasurement_nonExistingMeasurementTimestampReturnsStatus404AndNoMeasurementIsDeleted() {
        final Collection<Measurement> measurements = setupMeasurements2();
        final String deleteTimestamp = "2015-09-01T16:20:00.000Z";

        final Response delete = target("measurements/" + deleteTimestamp).request().delete();
        assertEquals("Status is 404", 404, delete.getStatus());

        //Verify that no measurement has been deleted
        measurements.forEach(m -> {
            final Response getUnmodified = target("measurements/" + m.getTimestamp()).request().get();
            assertEquals("Status is 200", 200, getUnmodified.getStatus());
            assertNotNull("Non matching measurement still exists", getUnmodified.readEntity(Measurement.class));
            assertBeforeAfter("DELETE", "unmodified", m.getTimestamp(), m.getMetrics());

        });

        final Response attemptedDelete = target("measurements/" + deleteTimestamp).request().get();
        assertEquals("Status is 404", 404, attemptedDelete.getStatus());
    }



    /**
     * Helper methods.
     */
    private void assertBeforeAfter(final String mode, final String change, final String timestamp, final Map<String, Float> metricsToAssert) {
        final Measurement m = target("measurements/" + timestamp).request().get(Measurement.class);
        final Map<String, Float> metricsToAssertAgainst = m.getMetrics();
        metricsToAssert.entrySet().forEach(e ->
                assertEquals(
                        String.format("%s: %s is %s", mode, e.getKey(), change),
                        e.getValue(),
                        metricsToAssertAgainst.get(e.getKey()),
                        0f
                ));
    }

    private Collection<Measurement> setupMeasurements1() {
        final ImmutableList.Builder<Measurement> builder = ImmutableList.builder();
        builder.add(new Measurement("2015-09-01T16:00:00.000Z", createMetrics(27.1f, 16.7f, 0f)));
        builder.add(new Measurement("2015-09-01T16:10:00.000Z", createMetrics(27.3f, 16.9f, 0f)));
        builder.add(new Measurement("2015-09-01T16:20:00.000Z", createMetrics(27.5f, 17.1f, 0f)));
        builder.add(new Measurement("2015-09-01T16:30:00.000Z", createMetrics(27.4f, 17.3f, 0f)));
        builder.add(new Measurement("2015-09-01T16:40:00.000Z", createMetrics(27.2f, 17.2f, 0f)));
        builder.add(new Measurement("2015-09-02T16:00:00.000Z", createMetrics(28.1f, 18.3f, 0f)));
        final List<Measurement> measurements = builder.build();
        measurements.forEach(m -> target("measurements").request().post(Entity.json(m)));
        return measurements;
    }

    private Collection<Measurement> setupMeasurements2() {
        final ImmutableList.Builder<Measurement> builder = ImmutableList.builder();
        builder.add(new Measurement("2015-09-01T16:00:00.000Z", createMetrics(27.1f, 16.7f, 0f)));
        builder.add(new Measurement("2015-09-01T16:10:00.000Z", createMetrics(27.3f, 16.9f, 0f)));
        final List<Measurement> measurements = builder.build();
        measurements.forEach(m -> target("measurements").request().post(Entity.json(m)));
        return measurements;
    }

    private static Map<String, Object> createObjectMetrics(final Object temperature, final Object dewPoint, final Object precipitation) {
        return ImmutableMap.of(Constants.TEMPERATURE, temperature, Constants.DEW_POINT, dewPoint, Constants.PRECIPITATION, precipitation);
    }

    private static Map<String, Float> createMetrics(final Float temperature, final Float dewPoint, final Float precipitation) {
        return ImmutableMap.of(Constants.TEMPERATURE, temperature, Constants.DEW_POINT, dewPoint, Constants.PRECIPITATION, precipitation);
    }

    private static Map<String, Object> createMeasurementMap(final String timestamp, final Map<String, Object> metrics) {
        final Map<String, Object> measurement = Maps.newHashMap();
        measurement.put(MEASUREMENT_KEY_TIMESTAMP, timestamp);
        measurement.put(MEASUREMENT_KEY_METRICS, ImmutableMap.copyOf(metrics));
        return ImmutableMap.copyOf(measurement);
    }

    private static Map<String, Object> createMeasurementMap(final Map<String, Object> metrics) {
        final Map<String, Object> measurement = Maps.newHashMap();
        measurement.put(MEASUREMENT_KEY_METRICS, ImmutableMap.copyOf(metrics));
        return ImmutableMap.copyOf(measurement);
    }

    /**
     * Intended to access the protected level method of MeasurementsServiceImpl.clear() to reset the
     * store to empty for test purposes.
     */
    private class ResettableMeasurementsService extends MeasurementsServiceImpl {

        private void resetToEmpty() {
            this.clearAll();
        }
    }
}
