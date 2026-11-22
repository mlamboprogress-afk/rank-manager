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
import za.co.taxirankmanager.adapters.in.rest.dto.CreateRouteRequest;
import za.co.taxirankmanager.adapters.in.rest.dto.RouteResponse;
import za.co.taxirankmanager.route.application.command.CreateRouteCommand;
import za.co.taxirankmanager.route.application.port.in.CreateRouteUseCase;
import za.co.taxirankmanager.route.application.port.in.ListRoutesUseCase;
import za.co.taxirankmanager.route.domain.RouteId;

import java.util.List;
import java.util.UUID;

@Path("/api/routes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Routes")
public class RouteResource {

    @Inject
    CreateRouteUseCase createRouteUseCase;

    @Inject
    ListRoutesUseCase listRoutesUseCase;

    @GET
    @Operation(summary = "List all active routes")
    public List<RouteResponse> list() {
        return listRoutesUseCase.listActiveRoutes().stream()
                .map(RouteResponse::from)
                .toList();
    }

    @POST
    @Transactional
    @Operation(summary = "Create a new route")
    public Response create(CreateRouteRequest request) {
        RouteId id = createRouteUseCase.createRoute(
                new CreateRouteCommand(request.name(), request.origin(), request.destination()));
        return Response.status(Response.Status.CREATED)
                .entity(new java.util.LinkedHashMap<String, String>() {{ put("id", id.value().toString()); }})
                .build();
    }

    @GET
    @Path("/{routeId}")
    @Operation(summary = "Get a route by id")
    public Response findById(@PathParam("routeId") UUID routeId) {
        return listRoutesUseCase.findRouteById(RouteId.of(routeId))
                .map(r -> Response.ok(RouteResponse.from(r)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
}
