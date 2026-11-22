package za.co.taxirankmanager.queue.domain;

public enum QueueEventType {
    DAILY_QUEUE_CREATED,
    DAILY_QUEUE_PUBLISHED,
    DAILY_QUEUE_CANCELLED,
    DAILY_QUEUE_COMPLETED,
    TAXI_ADDED_TO_QUEUE,
    TAXI_DEPARTED,
    TAXI_SKIPPED,
    QUEUE_REORDERED
}
