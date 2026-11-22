package za.co.taxirankmanager.route.domain;

import java.util.Objects;
import java.util.UUID;

public record RouteId(UUID value) {

    public RouteId {
        Objects.requireNonNull(value, "RouteId value must not be null");
    }

    public static RouteId of(UUID value) {
        return new RouteId(value);
    }

    public static RouteId of(String value) {
        return new RouteId(UUID.fromString(value));
    }

    public static RouteId generate() {
        return new RouteId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
