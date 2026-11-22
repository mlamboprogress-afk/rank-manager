package za.co.taxirankmanager.rank.application.command;

import java.util.Objects;

public record CreateRankCommand(
        String name,
        String locationDescription
) {
    public CreateRankCommand {
        Objects.requireNonNull(name, "name must not be null");
    }
}
