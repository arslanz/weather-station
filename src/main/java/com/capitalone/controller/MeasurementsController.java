package com.capitalone.controller;


import com.capitalone.controller.httpm.PATCH;
import com.capitalone.exception.EntityExistsException;
import com.capitalone.exception.EntityNotFoundException;
import com.capitalone.exception.client.BadRequestException;
import com.capitalone.exception.client.ConflictException;
import com.capitalone.exception.client.NotFoundException;
import com.capitalone.model.Measurement;
import com.capitalone.service.MeasurementsService;
import com.capitalone.service.MeasurementsServiceImpl;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

/**
 * A controller class that is responsible for setting up and handling the routes
 * belonging to 'measurements'.
 */
@Slf4j
@Path("/measurements")
public class MeasurementsController {
    private final MeasurementsService measurementsService;

    public MeasurementsController() {
        this(new MeasurementsServiceImpl());
    }

    public MeasurementsController(final MeasurementsService measurementsService) {
        this.measurementsService = measurementsService;
    }

	@POST
    @Consumes({MediaType.APPLICATION_JSON})
    public Response postMeasurement(
            @NotNull(message = "Measurement cannot be null.")
            @Valid final Measurement m
    ) throws URISyntaxException {
		try {
            log.info("Creating new measurement: {}", m);
            measurementsService.create(m);
            log.info("Done: Creating new measurement: {}", m);
            return Response.temporaryRedirect(new URI("measurements/" + m.getTimestamp())).status(Response.Status.CREATED).build();
        } catch (EntityExistsException e) {
            //Wrap EntityExistsException inside a 400 BAD_REQUEST exception so that it can be handled correctly by server
            throw new BadRequestException("Cannot create. Measurement already exists with timestamp: " + m.getTimestamp(), e);
        }
	}

	@GET
	@Path("/{timestamp : \\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}Z}")
    @Produces({MediaType.APPLICATION_JSON})
    public Measurement getMeasurementAtTimestamp(@PathParam("timestamp") final String timestamp) {
        log.info("Get measurement with timestamp: {}", timestamp);
        final Measurement m;
        try {
            m = measurementsService.retrieve(timestamp);
            log.info("Done: Get measurement with timestamp: {} - {}", timestamp, m);
        } catch (EntityNotFoundException e) {
            //Wrap EntityNotFoundException inside a 404 NOT_FOUND exception so that it can be handled correctly by server
            throw new NotFoundException("Could not retrieve. Measurement not found with timestamp: " + timestamp, e);
        }

        return m;
    }

	@GET
	@Path("/{date : \\d{4}-\\d{2}-\\d{2}}")
    @Produces({MediaType.APPLICATION_JSON})
    public Collection<Measurement> getMeasurementsOnDate(@PathParam("date") final String date) {
        log.info("Get all measurements with date: {}", date);
        final Collection<Measurement> retrieveResult = measurementsService.retrieveAll(date);

        //If no result then throw the NotFoundException to flag this as a status code 404 - NOT_FOUND
        if (retrieveResult == null || retrieveResult.isEmpty())
            throw new NotFoundException("No measurements found for date: " + date);

        log.info("Done: Get all measurements with date: {}", date);
        return retrieveResult;
	}

	@PUT
    @Path("/{timestamp : \\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}Z}")
	public void putMeasurement(
            @NotNull(message = "Measurement cannot be null.")
            @Valid final Measurement m,
            @PathParam("timestamp") final String timestamp
    ) {
        log.info("Put measurement with timestamp: {} and measurement: {}", timestamp, m);
        //Check if timestamp param and Measurement object timestamp are equal otherwise throw appropriate WebApplicationException
        if (!timestamp.equals(m.getTimestamp()))
            throw new ConflictException(String.format("Timestamp mismatch. Timestamp param (%s) should match Measurement.timestamp (%s).", timestamp, m.getTimestamp()));

        try {
            //Try to perform the put operation
            final Measurement updateResult = measurementsService.updateWhole(m);
            log.info("Done: Put measurement with timestamp: {} and measurement: {}. Result: {}", timestamp, m, updateResult);
        } catch (final EntityNotFoundException e) {
            //Wrap EntityNotFoundException inside a 404 exception so that it can be handled correctly by server
            throw new NotFoundException("Cannot update. Measurement not found with timestamp: " +  timestamp, e);
        }
    }

	@PATCH
    @Path("/{timestamp : \\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}Z}")
	public void patchMeasurement(
            @NotNull(message = "Measurement cannot be null.") @Valid final Measurement m,
            @PathParam("timestamp") final String timestamp
    ) {
        log.info("Patch measurement with timestamp: {} and measurement-delta: {}", timestamp, m);
        //Check if timestamp param and Measurement object timestamp are equal...
        if (!timestamp.equals(m.getTimestamp()))
            //...otherwise throw ConflictException
            throw new ConflictException("Timestamp mismatch. Timestamp param ("+timestamp+") should match Measurement.timestamp ("+m.getTimestamp()+").");

        try {
            //Try to perform the patch operation
            final Measurement updateResult = measurementsService.updatePartial(m);
            log.info("Done: Patch measurement with timestamp: {} and measurement-delta: {}. Result: {}", timestamp, m, updateResult);
        } catch (final EntityNotFoundException e) {
            //Wrap EntityNotFoundException inside a 404 NOT_FOUND exception so that it can be handled correctly by server
            throw new NotFoundException("Cannot update. Measurement not found with timestamp: " + timestamp, e);
        }
	}

	@DELETE
    @Path("/{timestamp : \\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}Z}")
	public void deleteMeasurement(@PathParam("timestamp") final String timestamp) {
        log.info("Delete measurement with timestamp: {}", timestamp);
        try {
            //Try to perform the delete operation
            final Measurement deleteResult = measurementsService.delete(timestamp);
            log.info("Done: Delete measurement with timestamp: {}, {}", timestamp, deleteResult);
        } catch (final EntityNotFoundException e) {
            //Wrap EntityNotFoundException inside a 404 NOT_FOUND exception so that it can be handled correctly by server
            throw new NotFoundException("Could not delete. Measurement not found with timestamp: " + timestamp, e);
        }
	}
}
