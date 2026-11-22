package za.co.taxirankmanager.queue.domain;

import java.util.Objects;
import java.util.UUID;

public record DailyQueueId(UUID value) {

    public DailyQueueId {
        Objects.requireNonNull(value, "DailyQueueId value must not be null");
    }

    public static DailyQueueId of(UUID value) {
        return new DailyQueueId(value);
    }

    public static DailyQueueId of(String value) {
        return new DailyQueueId(UUID.fromString(value));
    }

    public static DailyQueueId generate() {
        return new DailyQueueId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
