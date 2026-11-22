package za.co.taxirankmanager.queue.domain;

import za.co.taxirankmanager.identity.domain.UserId;

import java.time.Instant;

/**
 * Immutable audit record of a single queue operation.
 * Null fields are intentional — not every event has an entry, reason or position change.
 */
public record QueueEvent(
        QueueEventId id,
        DailyQueueId queueId,
        QueueEntryId entryId,
        QueueEventType eventType,
        QueueEntryStatus previousStatus,
        QueueEntryStatus newStatus,
        Integer previousPosition,
        Integer newPosition,
        String reason,
        UserId actorUserId,
        Instant occurredAt
) {

    public static QueueEvent queueCreated(DailyQueueId queueId, UserId actor, Instant now) {
        return new QueueEvent(
                QueueEventId.generate(), queueId, null,
                QueueEventType.DAILY_QUEUE_CREATED,
                null, null, null, null, null, actor, now);
    }

    public static QueueEvent queuePublished(DailyQueueId queueId, UserId actor, Instant now) {
        return new QueueEvent(
                QueueEventId.generate(), queueId, null,
                QueueEventType.DAILY_QUEUE_PUBLISHED,
                null, null, null, null, null, actor, now);
    }

    public static QueueEvent taxiAdded(DailyQueueId queueId, QueueEntryId entryId, int position, UserId actor, Instant now) {
        return new QueueEvent(
                QueueEventId.generate(), queueId, entryId,
                QueueEventType.TAXI_ADDED_TO_QUEUE,
                null, QueueEntryStatus.WAITING, null, position, null, actor, now);
    }

    public static QueueEvent taxiDeparted(DailyQueueId queueId, QueueEntryId entryId,
                                           QueueEntryStatus prevStatus, int position,
                                           UserId actor, Instant now) {
        return new QueueEvent(
                QueueEventId.generate(), queueId, entryId,
                QueueEventType.TAXI_DEPARTED,
                prevStatus, QueueEntryStatus.DEPARTED, position, position, null, actor, now);
    }

    public static QueueEvent taxiSkipped(DailyQueueId queueId, QueueEntryId entryId,
                                          QueueEntryStatus prevStatus,
                                          int prevPosition, int newPosition,
                                          UserId actor, String reason, Instant now) {
        return new QueueEvent(
                QueueEventId.generate(), queueId, entryId,
                QueueEventType.TAXI_SKIPPED,
                prevStatus, QueueEntryStatus.SKIPPED,
                prevPosition, newPosition, reason, actor, now);
    }

    public static QueueEvent queueCancelled(DailyQueueId queueId, String reason, UserId actor, Instant now) {
        return new QueueEvent(
                QueueEventId.generate(), queueId, null,
                QueueEventType.DAILY_QUEUE_CANCELLED,
                null, null, null, null, reason, actor, now);
    }
}
