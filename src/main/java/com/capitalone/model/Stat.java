package com.capitalone.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

/**
 * This class represents the statistic response to the client.
 */
@Value
public class Stat {
    private final String metric;
    private final StatType stat;
    private final float value;

    @JsonCreator
    public Stat(
            @JsonProperty("metric") final String metric,
            @JsonProperty("stat") final StatType stat,
            @JsonProperty("value") final float value) {
        this.metric = metric;
        this.stat = stat;
        this.value = value;
    }
}
