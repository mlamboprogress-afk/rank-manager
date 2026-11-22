package za.co.taxirankmanager.fleet.application.command;

import java.util.Objects;

public record RegisterTaxiCommand(String registration) {
    public RegisterTaxiCommand {
        Objects.requireNonNull(registration, "registration must not be null");
    }
}
