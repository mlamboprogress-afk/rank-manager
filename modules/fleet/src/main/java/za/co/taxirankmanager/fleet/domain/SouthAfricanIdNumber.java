package za.co.taxirankmanager.fleet.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

/**
 * South African 13-digit ID number.
 * Validates format and Luhn checksum.
 * toString() masks all but the last 4 digits to prevent accidental exposure in logs.
 */
public final class SouthAfricanIdNumber {

    private final String value;

    public static SouthAfricanIdNumber of(String value) {
        return new SouthAfricanIdNumber(value);
    }

    public SouthAfricanIdNumber(String value) {
        Objects.requireNonNull(value, "SA ID number must not be null");
        validate(value);
        this.value = value;
    }

    private static void validate(String idNumber) {
        if (!idNumber.matches("\\d{13}")) {
            throw new IllegalArgumentException("SA ID number must be exactly 13 digits");
        }
        validateDatePortion(idNumber);
        int citizenshipDigit = Character.getNumericValue(idNumber.charAt(10));
        if (citizenshipDigit != 0 && citizenshipDigit != 1) {
            throw new IllegalArgumentException("SA ID number has an invalid citizenship digit");
        }
        if (!passesLuhn(idNumber)) {
            throw new IllegalArgumentException("SA ID number has an invalid checksum");
        }
    }

    private static void validateDatePortion(String idNumber) {
        int month = Integer.parseInt(idNumber.substring(2, 4));
        int day = Integer.parseInt(idNumber.substring(4, 6));
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("SA ID number has an invalid month");
        }
        if (day < 1 || day > 31) {
            throw new IllegalArgumentException("SA ID number has an invalid day");
        }
    }

    private static boolean passesLuhn(String idNumber) {
        int sum = 0;
        boolean alternate = false;
        for (int i = idNumber.length() - 1; i >= 0; i--) {
            int n = Character.getNumericValue(idNumber.charAt(i));
            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }

    public String getValue() {
        return value;
    }

    /** SHA-256 hex of the raw value — safe for DB lookup without storing the plaintext. */
    public String hash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** Returns the last 4 digits only. Use this for display. */
    public String masked() {
        return "*********" + value.substring(9);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SouthAfricanIdNumber other)) return false;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    /** Always masked — never exposes the full ID number. */
    @Override
    public String toString() {
        return masked();
    }
}
