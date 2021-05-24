package com.capitalone.controller;


import com.capitalone.controller.param.FromToDateTimeParam;
import com.capitalone.exception.client.BadRequestException;
import com.capitalone.model.Stat;
import com.capitalone.model.StatType;
import com.capitalone.service.StatsService;
import com.capitalone.service.StatsServiceImpl;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;

import static com.capitalone.model.StatType.UNSUPPORTED;


/**
 * A controller class that is responsible for retrieving statistics for the end point 'stats'.
 */

@Slf4j
@Path("/stats")
public class StatsController {
    private final StatsService statsService;

    public StatsController() {
        this(new StatsServiceImpl());
    }

    public StatsController(final StatsService statsService) {
        this.statsService = statsService;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
	public List<Stat> get(
			@QueryParam("stat") final List<String> statTypes,
			@QueryParam("metric") final List<String> metrics,
            @Valid @QueryParam("fromDateTime") final FromToDateTimeParam fromDateTime,
            @Valid @QueryParam("toDateTime") final FromToDateTimeParam toDateTime
	) {
        final String prettyStatTypes = Arrays.toString(statTypes.toArray());
        final String prettyMetrics = Arrays.toString(metrics.toArray());
        log.info("Getting statTypes{} and metrics{} from {} until {}", prettyStatTypes, prettyMetrics, fromDateTime, toDateTime);
        final List<Stat> result = collect(toStatTypes(statTypes), metrics, fromDateTime.getTimestamp(), toDateTime.getTimestamp());
        log.info("Done: Getting statTypes[{}] and metrics[{}] from {} until {}", prettyStatTypes, prettyMetrics, fromDateTime, toDateTime);
		return result;
	}

    private List<Stat> collect(final List<StatType> statTypes, final List<String> metrics, final String fromDateTime, final String toDateTime) {
        final List<Stat> response = Lists.newArrayListWithCapacity(statTypes.size() * metrics.size());
        for (final String metric: metrics) {
            for (final StatType statType : statTypes) {
                switch (statType) {
                    case AVERAGE:
                        final Stat average = statsService.getAverage(metric, fromDateTime, toDateTime);
                        if (average != null)
                            response.add(average);
                        break;
                    case MAX:
                        final Stat max = statsService.getMax(metric, fromDateTime, toDateTime);
                        if (max != null)
                            response.add(max);
                        break;
                    case MIN:
                        final Stat min = statsService.getMin(metric, fromDateTime, toDateTime);
                        if (min != null)
                            response.add(min);
                        break;
                }
            }
        }
        return response;
    }

    private List<StatType> toStatTypes(final List<String> stats) {
        final List<StatType> statTypes = Lists.newArrayList();
        for (final String s : stats) {
            final StatType statType = StatType.fromString(s);
            if (statType == UNSUPPORTED) {
                log.error("Unsupported StatType passed: {}", s);
                throw new BadRequestException("Unsupported StatType: " + s);
            }
            statTypes.add(statType);
        }
        return statTypes;
    }
}
