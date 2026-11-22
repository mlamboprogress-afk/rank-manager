package za.co.taxirankmanager.route.application.command;

import za.co.taxirankmanager.rank.domain.RankId;
import za.co.taxirankmanager.route.domain.RouteId;

import java.util.Objects;

public record AssignTaxiToRankRouteCommand(
        RankId rankId,
        RouteId routeId
) {
    public AssignTaxiToRankRouteCommand {
        Objects.requireNonNull(rankId, "rankId must not be null");
        Objects.requireNonNull(routeId, "routeId must not be null");
    }
}
