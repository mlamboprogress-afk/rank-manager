package za.co.taxirankmanager.adapters.in.rest;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import za.co.taxirankmanager.adapters.in.rest.dto.AddQueueEntryRequest;
import za.co.taxirankmanager.adapters.in.rest.dto.CreateQueueRequest;
import za.co.taxirankmanager.adapters.in.rest.dto.DepartTaxiRequest;
import za.co.taxirankmanager.adapters.in.rest.dto.QueueResponse;
import za.co.taxirankmanager.adapters.in.rest.dto.SkipTaxiRequest;
import za.co.taxirankmanager.queue.application.command.AddEntryToQueueCommand;
import za.co.taxirankmanager.queue.application.command.CreateDailyQueueCommand;
import za.co.taxirankmanager.queue.application.command.DepartTaxiCommand;
import za.co.taxirankmanager.queue.application.command.PublishDailyQueueCommand;
import za.co.taxirankmanager.queue.application.command.SkipTaxiCommand;
import za.co.taxirankmanager.queue.application.port.in.AddEntryToQueueUseCase;
import za.co.taxirankmanager.queue.application.port.in.CreateDailyQueueUseCase;
import za.co.taxirankmanager.queue.application.port.in.DepartTaxiUseCase;
import za.co.taxirankmanager.queue.application.port.in.GetQueueUseCase;
import za.co.taxirankmanager.queue.application.port.in.PublishDailyQueueUseCase;
import za.co.taxirankmanager.queue.application.port.in.SkipTaxiUseCase;
import za.co.taxirankmanager.queue.domain.DailyQueueId;
import za.co.taxirankmanager.queue.domain.QueueEntryId;
import za.co.taxirankmanager.fleet.domain.TaxiId;
import za.co.taxirankmanager.route.domain.RankRouteId;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Path("/api/queues")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Queues")
public class QueueResource {

    @Inject
    CreateDailyQueueUseCase createQueueUseCase;

    @Inject
    PublishDailyQueueUseCase publishQueueUseCase;

    @Inject
    AddEntryToQueueUseCase addEntryUseCase;

    @Inject
    SkipTaxiUseCase skipTaxiUseCase;

    @Inject
    DepartTaxiUseCase departTaxiUseCase;

    @Inject
    GetQueueUseCase getQueueUseCase;

    @POST
    @Transactional
    @Operation(summary = "Create a daily queue")
    public Response create(CreateQueueRequest request) {
        DailyQueueId id = createQueueUseCase.createQueue(new CreateDailyQueueCommand(
                RankRouteId.of(UUID.fromString(request.rankRouteId())),
                LocalDate.parse(request.serviceDate())));
        return Response.status(Response.Status.CREATED)
                .entity(new java.util.LinkedHashMap<String, String>() {{ put("id", id.value().toString()); }})
                .build();
    }

    @GET
    @Path("/{queueId}")
    @Operation(summary = "Get a queue by id")
    public Response findById(@PathParam("queueId") UUID queueId) {
        return getQueueUseCase.findById(DailyQueueId.of(queueId))
                .map(q -> Response.ok(QueueResponse.from(q)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Operation(summary = "Find queue by rank-route and service date")
    public Response findByRankRouteAndDate(
            @QueryParam("rankRouteId") UUID rankRouteId,
            @QueryParam("date") String date) {
        return getQueueUseCase.findByRankRouteAndDate(RankRouteId.of(rankRouteId), LocalDate.parse(date))
                .map(q -> Response.ok(QueueResponse.from(q)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/by-rank-route/{rankRouteId}")
    @Operation(summary = "List all queues for a rank-route")
    public List<QueueResponse> listByRankRoute(@PathParam("rankRouteId") UUID rankRouteId) {
        return getQueueUseCase.listByRankRoute(RankRouteId.of(rankRouteId)).stream()
                .map(QueueResponse::from)
                .toList();
    }

    @PUT
    @Path("/{queueId}/publish")
    @Transactional
    @Operation(summary = "Publish a draft queue")
    public Response publish(@PathParam("queueId") UUID queueId,
                            @QueryParam("expectedVersion") long expectedVersion) {
        publishQueueUseCase.publishQueue(new PublishDailyQueueCommand(DailyQueueId.of(queueId), expectedVersion));
        return Response.noContent().build();
    }

    @POST
    @Path("/{queueId}/entries")
    @Transactional
    @Operation(summary = "Add a taxi to the queue")
    public Response addEntry(@PathParam("queueId") UUID queueId, AddQueueEntryRequest request) {
        QueueEntryId entryId = addEntryUseCase.addEntry(new AddEntryToQueueCommand(
                DailyQueueId.of(queueId),
                TaxiId.of(UUID.fromString(request.taxiId())),
                request.expectedVersion()));
        return Response.status(Response.Status.CREATED)
                .entity(new java.util.LinkedHashMap<String, String>() {{ put("entryId", entryId.value().toString()); }})
                .build();
    }

    @PUT
    @Path("/{queueId}/entries/{entryId}/skip")
    @Transactional
    @Operation(summary = "Skip a taxi in the queue")
    public Response skip(@PathParam("queueId") UUID queueId,
                         @PathParam("entryId") UUID entryId,
                         SkipTaxiRequest request) {
        skipTaxiUseCase.skipTaxi(new SkipTaxiCommand(
                DailyQueueId.of(queueId),
                QueueEntryId.of(entryId),
                request.reason(),
                request.expectedVersion()));
        return Response.noContent().build();
    }

    @PUT
    @Path("/{queueId}/entries/{entryId}/depart")
    @Transactional
    @Operation(summary = "Mark a taxi as departed")
    public Response depart(@PathParam("queueId") UUID queueId,
                           @PathParam("entryId") UUID entryId,
                           DepartTaxiRequest request) {
        departTaxiUseCase.departTaxi(new DepartTaxiCommand(
                DailyQueueId.of(queueId),
                QueueEntryId.of(entryId),
                request.expectedVersion()));
        return Response.noContent().build();
    }
}
