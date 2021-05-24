package com.capitalone.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import lombok.Value;
import org.hibernate.validator.constraints.NotBlank;

import java.util.Map;

/**
 * This class represents the Measurement data. For flexibility in creating new measurement devices,
 * the metrics are stored as String (metric name) to Float (metric value) map.
 */
@Value
public class Measurement {
    //The expected type is string and the format is: 2016-09-01T16:00:00.000Z
    @NotBlank(message = "The timestamp must be specified.")
    private final String timestamp;
    private final Map<String, Float> metrics;

    @JsonCreator
    public Measurement(
            @JsonProperty("timestamp") final String timestamp,
            @JsonProperty("metrics") final Map<String, Float> metrics) {
        this.timestamp = timestamp;
        this.metrics = ImmutableMap.copyOf(metrics);
    }
}
