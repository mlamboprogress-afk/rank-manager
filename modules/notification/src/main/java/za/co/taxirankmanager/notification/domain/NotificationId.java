package za.co.taxirankmanager.notification.domain;

import java.util.Objects;
import java.util.UUID;

public record NotificationId(UUID value) {

    public NotificationId {
        Objects.requireNonNull(value, "NotificationId value must not be null");
    }

    public static NotificationId of(UUID value) {
        return new NotificationId(value);
    }

    public static NotificationId generate() {
        return new NotificationId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
