package za.co.taxirankmanager.fleet.domain;

import java.util.Objects;
import java.util.UUID;

public record DriverId(UUID value) {

    public DriverId {
        Objects.requireNonNull(value, "DriverId value must not be null");
    }

    public static DriverId of(UUID value) {
        return new DriverId(value);
    }

    public static DriverId of(String value) {
        return new DriverId(UUID.fromString(value));
    }

    public static DriverId generate() {
        return new DriverId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
