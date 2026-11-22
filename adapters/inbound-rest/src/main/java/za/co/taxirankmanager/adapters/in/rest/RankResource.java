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
import za.co.taxirankmanager.adapters.in.rest.dto.AssignRankRouteRequest;
import za.co.taxirankmanager.adapters.in.rest.dto.CreateRankRequest;
import za.co.taxirankmanager.adapters.in.rest.dto.RankResponse;
import za.co.taxirankmanager.adapters.in.rest.dto.RouteResponse;
import za.co.taxirankmanager.rank.application.command.CreateRankCommand;
import za.co.taxirankmanager.rank.application.port.in.CreateRankUseCase;
import za.co.taxirankmanager.rank.application.port.in.ListRanksUseCase;
import za.co.taxirankmanager.rank.domain.RankId;
import za.co.taxirankmanager.route.application.command.AssignTaxiToRankRouteCommand;
import za.co.taxirankmanager.route.application.port.in.AssignTaxiToRankRouteUseCase;
import za.co.taxirankmanager.route.application.port.in.ListRoutesUseCase;
import za.co.taxirankmanager.route.domain.RouteId;

import java.util.List;
import java.util.UUID;

@Path("/api/ranks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Ranks")
public class RankResource {

    @Inject
    CreateRankUseCase createRankUseCase;

    @Inject
    ListRanksUseCase listRanksUseCase;

    @Inject
    AssignTaxiToRankRouteUseCase assignTaxiToRankRouteUseCase;

    @Inject
    ListRoutesUseCase listRoutesUseCase;

    @GET
    @Operation(summary = "List all active ranks")
    public List<RankResponse> list() {
        return listRanksUseCase.listActive().stream()
                .map(RankResponse::from)
                .toList();
    }

    @POST
    @Transactional
    @Operation(summary = "Create a new rank")
    public Response create(CreateRankRequest request) {
        RankId id = createRankUseCase.createRank(new CreateRankCommand(request.name(), request.locationDescription()));
        return Response.status(Response.Status.CREATED)
                .entity(new java.util.LinkedHashMap<String, String>() {{ put("id", id.value().toString()); }})
                .build();
    }

    @GET
    @Path("/{rankId}")
    @Operation(summary = "Get a rank by id")
    public Response findById(@PathParam("rankId") UUID rankId) {
        return listRanksUseCase.findById(RankId.of(rankId))
                .map(r -> Response.ok(RankResponse.from(r)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/{rankId}/routes")
    @Operation(summary = "List routes assigned to a rank")
    public List<RouteResponse> listRankRoutes(@PathParam("rankId") UUID rankId) {
        return listRoutesUseCase.findRankRoutesByRank(RankId.of(rankId)).stream()
                .map(rr -> listRoutesUseCase.findRouteById(rr.getRouteId()))
                .flatMap(java.util.Optional::stream)
                .map(RouteResponse::from)
                .toList();
    }

    @POST
    @Path("/rank-routes")
    @Transactional
    @Operation(summary = "Assign a route to a rank")
    public Response assignRoute(AssignRankRouteRequest request) {
        AssignTaxiToRankRouteCommand cmd = new AssignTaxiToRankRouteCommand(
                RankId.of(UUID.fromString(request.rankId())),
                RouteId.of(UUID.fromString(request.routeId())));
        var id = assignTaxiToRankRouteUseCase.assignTaxi(cmd);
        return Response.status(Response.Status.CREATED)
                .entity(new java.util.LinkedHashMap<String, String>() {{ put("id", id.value().toString()); }})
                .build();
    }
}
