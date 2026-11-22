package za.co.taxirankmanager.adapters.in.rest;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import za.co.taxirankmanager.adapters.in.rest.dto.DriverResponse;
import za.co.taxirankmanager.adapters.in.rest.dto.RegisterDriverRequest;
import za.co.taxirankmanager.adapters.in.rest.dto.RegisterTaxiRequest;
import za.co.taxirankmanager.adapters.in.rest.dto.TaxiResponse;
import za.co.taxirankmanager.fleet.application.command.RegisterDriverCommand;
import za.co.taxirankmanager.fleet.application.command.RegisterTaxiCommand;
import za.co.taxirankmanager.fleet.application.port.in.ListFleetUseCase;
import za.co.taxirankmanager.fleet.application.port.in.RegisterDriverUseCase;
import za.co.taxirankmanager.fleet.application.port.in.RegisterTaxiUseCase;
import za.co.taxirankmanager.fleet.domain.DriverId;
import za.co.taxirankmanager.fleet.domain.TaxiId;

import java.util.List;
import java.util.UUID;

@Path("/api/fleet")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Fleet")
public class FleetResource {

    @Inject
    RegisterTaxiUseCase registerTaxiUseCase;

    @Inject
    RegisterDriverUseCase registerDriverUseCase;

    @Inject
    ListFleetUseCase listFleetUseCase;

    @GET
    @Path("/taxis")
    @Operation(summary = "List all active taxis")
    public List<TaxiResponse> listTaxis() {
        return listFleetUseCase.listActiveTaxis().stream()
                .map(TaxiResponse::from)
                .toList();
    }

    @POST
    @Path("/taxis")
    @Transactional
    @Operation(summary = "Register a new taxi")
    public Response registerTaxi(RegisterTaxiRequest request) {
        TaxiId id = registerTaxiUseCase.registerTaxi(new RegisterTaxiCommand(request.registration()));
        return Response.status(Response.Status.CREATED)
                .entity(new java.util.LinkedHashMap<String, String>() {{ put("id", id.value().toString()); }})
                .build();
    }

    @GET
    @Path("/taxis/{taxiId}")
    @Operation(summary = "Get a taxi by id")
    public Response findTaxiById(@PathParam("taxiId") UUID taxiId) {
        return listFleetUseCase.findTaxiById(TaxiId.of(taxiId))
                .map(t -> Response.ok(TaxiResponse.from(t)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/drivers")
    @Operation(summary = "List all active drivers")
    public List<DriverResponse> listDrivers() {
        return listFleetUseCase.listActiveDrivers().stream()
                .map(DriverResponse::from)
                .toList();
    }

    @POST
    @Path("/drivers")
    @Transactional
    @Operation(summary = "Register a new driver")
    public Response registerDriver(RegisterDriverRequest request) {
        DriverId id = registerDriverUseCase.registerDriver(
                new RegisterDriverCommand(request.idNumber(), request.firstName(), request.lastName(), request.mobileNumber()));
        return Response.status(Response.Status.CREATED)
                .entity(new java.util.LinkedHashMap<String, String>() {{ put("id", id.value().toString()); }})
                .build();
    }

    @GET
    @Path("/drivers/{driverId}")
    @Operation(summary = "Get a driver by id")
    public Response findDriverById(@PathParam("driverId") UUID driverId) {
        return listFleetUseCase.findDriverById(DriverId.of(driverId))
                .map(d -> Response.ok(DriverResponse.from(d)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
}
