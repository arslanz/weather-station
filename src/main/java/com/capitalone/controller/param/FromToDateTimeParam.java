package com.capitalone.controller.param;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * A common date-time param class to allow bean validation in a single place.
 */
@Data
public final class FromToDateTimeParam {
    @NotBlank(message = "Both fromDateTime and toDateTime must be specified.")
    @Pattern(
            message = "Both fromDateTime and toDateTime must be in UTC format e.g. 2015-09-01T16:00:00.000Z",
            regexp="\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z"
    )
    private final String timestamp;

}
