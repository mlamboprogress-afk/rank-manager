package za.co.taxirankmanager.fleet.domain;

import java.util.Objects;

/**
 * South African vehicle registration number.
 * Equality is based on the normalised value so that "ABC 123 MP" and "abc123mp" are the same vehicle.
 */
public final class VehicleRegistration {

    private final String displayValue;
    private final String normalisedValue;

    private VehicleRegistration(String displayValue, String normalisedValue) {
        this.displayValue = displayValue;
        this.normalisedValue = normalisedValue;
    }

    public static VehicleRegistration of(String input) {
        Objects.requireNonNull(input, "Vehicle registration must not be null");
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Vehicle registration must not be blank");
        }
        String normalised = normalise(trimmed);
        if (normalised.isEmpty()) {
            throw new IllegalArgumentException("Vehicle registration contains no alphanumeric characters");
        }
        return new VehicleRegistration(trimmed, normalised);
    }

    private static String normalise(String input) {
        return input.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    public String getDisplayValue() { return displayValue; }
    public String getNormalisedValue() { return normalisedValue; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VehicleRegistration other)) return false;
        return normalisedValue.equals(other.normalisedValue);
    }

    @Override
    public int hashCode() {
        return normalisedValue.hashCode();
    }

    @Override
    public String toString() {
        return displayValue;
    }
}
