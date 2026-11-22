package za.co.taxirankmanager.route.application.command;

import java.util.Objects;

public record CreateRouteCommand(
        String name,
        String origin,
        String destination
) {
    public CreateRouteCommand {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(origin, "origin must not be null");
        Objects.requireNonNull(destination, "destination must not be null");
    }
}
