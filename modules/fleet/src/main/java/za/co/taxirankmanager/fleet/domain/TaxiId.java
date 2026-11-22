package za.co.taxirankmanager.fleet.domain;

import java.util.Objects;
import java.util.UUID;

public record TaxiId(UUID value) {

    public TaxiId {
        Objects.requireNonNull(value, "TaxiId value must not be null");
    }

    public static TaxiId of(UUID value) {
        return new TaxiId(value);
    }

    public static TaxiId of(String value) {
        return new TaxiId(UUID.fromString(value));
    }

    public static TaxiId generate() {
        return new TaxiId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
