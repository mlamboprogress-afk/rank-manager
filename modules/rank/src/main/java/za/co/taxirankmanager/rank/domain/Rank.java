package za.co.taxirankmanager.rank.domain;

import java.util.Objects;

public class Rank {

    private final RankId id;
    private String name;
    private String locationDescription;
    private boolean active;

    private Rank(RankId id, String name, String locationDescription, boolean active) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.locationDescription = locationDescription;
        this.active = active;
    }

    public static Rank create(RankId id, String name, String locationDescription) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Rank name must not be blank");
        }
        return new Rank(id, name.trim(), locationDescription, true);
    }

    public static Rank reconstitute(RankId id, String name, String locationDescription, boolean active) {
        return new Rank(id, name, locationDescription, active);
    }

    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Rank name must not be blank");
        }
        this.name = newName.trim();
    }

    public void deactivate() {
        this.active = false;
    }

    public RankId getId() { return id; }
    public String getName() { return name; }
    public String getLocationDescription() { return locationDescription; }
    public boolean isActive() { return active; }
}
