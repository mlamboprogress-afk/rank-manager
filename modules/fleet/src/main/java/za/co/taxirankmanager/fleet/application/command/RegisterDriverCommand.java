package za.co.taxirankmanager.fleet.application.command;

import java.util.Objects;

public record RegisterDriverCommand(
        String idNumber,
        String firstName,
        String lastName,
        String mobileNumber
) {
    public RegisterDriverCommand {
        Objects.requireNonNull(idNumber, "idNumber must not be null");
        Objects.requireNonNull(firstName, "firstName must not be null");
        Objects.requireNonNull(lastName, "lastName must not be null");
    }
}
