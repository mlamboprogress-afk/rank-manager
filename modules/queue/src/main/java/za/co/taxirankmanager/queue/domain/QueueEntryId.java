package za.co.taxirankmanager.queue.domain;

import java.util.Objects;
import java.util.UUID;

public record QueueEntryId(UUID value) {

    public QueueEntryId {
        Objects.requireNonNull(value, "QueueEntryId value must not be null");
    }

    public static QueueEntryId of(UUID value) {
        return new QueueEntryId(value);
    }

    public static QueueEntryId of(String value) {
        return new QueueEntryId(UUID.fromString(value));
    }

    public static QueueEntryId generate() {
        return new QueueEntryId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
