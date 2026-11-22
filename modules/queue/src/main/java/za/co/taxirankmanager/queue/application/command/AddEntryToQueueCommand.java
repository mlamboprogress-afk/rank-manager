package za.co.taxirankmanager.queue.application.command;

import za.co.taxirankmanager.fleet.domain.TaxiId;
import za.co.taxirankmanager.queue.domain.DailyQueueId;

import java.util.Objects;

public record AddEntryToQueueCommand(
        DailyQueueId queueId,
        TaxiId taxiId,
        long expectedVersion
) {
    public AddEntryToQueueCommand {
        Objects.requireNonNull(queueId, "queueId must not be null");
        Objects.requireNonNull(taxiId, "taxiId must not be null");
    }
}
