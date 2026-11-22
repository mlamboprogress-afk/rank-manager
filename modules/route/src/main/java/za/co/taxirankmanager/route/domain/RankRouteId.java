package za.co.taxirankmanager.route.domain;

import java.util.Objects;
import java.util.UUID;

public record RankRouteId(UUID value) {

    public RankRouteId {
        Objects.requireNonNull(value, "RankRouteId value must not be null");
    }

    public static RankRouteId of(UUID value) {
        return new RankRouteId(value);
    }

    public static RankRouteId of(String value) {
        return new RankRouteId(UUID.fromString(value));
    }

    public static RankRouteId generate() {
        return new RankRouteId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
