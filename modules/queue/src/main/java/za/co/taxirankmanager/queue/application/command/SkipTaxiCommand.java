package za.co.taxirankmanager.queue.application.command;

import za.co.taxirankmanager.queue.domain.DailyQueueId;
import za.co.taxirankmanager.queue.domain.QueueEntryId;

import java.util.Objects;

public record SkipTaxiCommand(
        DailyQueueId queueId,
        QueueEntryId entryId,
        String reason,
        long expectedVersion
) {
    public SkipTaxiCommand {
        Objects.requireNonNull(queueId, "queueId must not be null");
        Objects.requireNonNull(entryId, "entryId must not be null");
    }
}
