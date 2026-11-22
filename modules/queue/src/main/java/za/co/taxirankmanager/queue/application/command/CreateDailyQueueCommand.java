package za.co.taxirankmanager.queue.application.command;

import za.co.taxirankmanager.route.domain.RankRouteId;

import java.time.LocalDate;
import java.util.Objects;

public record CreateDailyQueueCommand(
        RankRouteId rankRouteId,
        LocalDate serviceDate
) {
    public CreateDailyQueueCommand {
        Objects.requireNonNull(rankRouteId, "rankRouteId must not be null");
        Objects.requireNonNull(serviceDate, "serviceDate must not be null");
    }
}
