package com.capitalone.controller.param;

import org.junit.Test;

import javax.validation.constraints.Pattern;
import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

/**
 * Tests to validate the regex within the annotation for field, timestamp.
 */
public class FromToDateTimeParamTest {

    @Test
    public void testCorrectlyFormedUtcTimestampMatchesRegex() throws Exception {
        timestampVerify("2015-09-01T16:00:00.000Z", true);
    }

    @Test
    public void testLowerCaseUtcTimestampDoesNotMatchRegex() throws Exception {
        timestampVerify("2015-09-01t16:00:00.000z", false);
        timestampVerify("2015-09-01t16:00:00.000Z", false);
        timestampVerify("2015-09-01T16:00:00.000z", false);
    }

    @Test
    public void testMissingZDoesNotMatchTimestampRegex() throws Exception {
        timestampVerify("2015-09-01t16:00:00.000", false);
        timestampVerify("2015-09-01t16:00:00.000", false);
        timestampVerify("2015-09-01T16:00:00.000", false);
    }

    @Test
    public void testMissingLeadingZerosDoesNotMatchTimestampRegex() throws Exception {
        timestampVerify("2015-9-1t16:00:00.000", false);
        timestampVerify("2015-09-01t7:00:00.000", false);
    }

    @Test
    public void testEdgeCasesMatchTimestampRegex() throws Exception {
        timestampVerify("0000-00-00T00:00:00.000Z", true);
        timestampVerify("9999-99-99T99:99:99.999Z", true);
    }

    private void timestampVerify(final String timestamp, final boolean shouldMatch) throws NoSuchFieldException {
        // Use reflection to get the regex within the field, 'timestamp', which is annotated with @Pattern
        final Field field = FromToDateTimeParam.class.getDeclaredField("timestamp");
        final Pattern[] annotations = field.getAnnotationsByType(Pattern.class);
        final String timestampRegex = annotations[0].regexp();
        assertEquals(timestamp.matches(timestampRegex), shouldMatch);

    }
}