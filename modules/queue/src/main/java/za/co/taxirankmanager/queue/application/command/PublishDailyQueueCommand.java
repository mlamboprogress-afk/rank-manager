package za.co.taxirankmanager.queue.application.command;

import za.co.taxirankmanager.queue.domain.DailyQueueId;

import java.util.Objects;

public record PublishDailyQueueCommand(
        DailyQueueId queueId,
        long expectedVersion
) {
    public PublishDailyQueueCommand {
        Objects.requireNonNull(queueId, "queueId must not be null");
    }
}
