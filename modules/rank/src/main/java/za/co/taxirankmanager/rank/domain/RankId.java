package za.co.taxirankmanager.rank.domain;

import java.util.Objects;
import java.util.UUID;

public record RankId(UUID value) {

    public RankId {
        Objects.requireNonNull(value, "RankId value must not be null");
    }

    public static RankId of(UUID value) {
        return new RankId(value);
    }

    public static RankId of(String value) {
        return new RankId(UUID.fromString(value));
    }

    public static RankId generate() {
        return new RankId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
