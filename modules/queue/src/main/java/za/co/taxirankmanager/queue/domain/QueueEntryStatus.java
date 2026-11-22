package za.co.taxirankmanager.queue.domain;

/**
 * Exactly three statuses — there is no CALLED status.
 * Calling a taxi is a notification event, not a queue-entry status change.
 */
public enum QueueEntryStatus {
    WAITING,
    SKIPPED,
    DEPARTED
}
