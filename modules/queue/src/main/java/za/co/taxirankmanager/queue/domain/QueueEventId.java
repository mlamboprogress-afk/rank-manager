package za.co.taxirankmanager.queue.domain;

import java.util.Objects;
import java.util.UUID;

public record QueueEventId(UUID value) {

    public QueueEventId {
        Objects.requireNonNull(value, "QueueEventId value must not be null");
    }

    public static QueueEventId generate() {
        return new QueueEventId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
