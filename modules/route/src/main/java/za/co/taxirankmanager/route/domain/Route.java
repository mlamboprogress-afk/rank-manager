package za.co.taxirankmanager.route.domain;

import java.util.Objects;

public class Route {

    private final RouteId id;
    private String name;
    private String origin;
    private String destination;
    private boolean active;

    private Route(RouteId id, String name, String origin, String destination, boolean active) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.origin = Objects.requireNonNull(origin);
        this.destination = Objects.requireNonNull(destination);
        this.active = active;
    }

    public static Route create(RouteId id, String name, String origin, String destination) {
        return new Route(id, name, origin, destination, true);
    }

    public static Route reconstitute(RouteId id, String name, String origin, String destination, boolean active) {
        return new Route(id, name, origin, destination, active);
    }

    public RouteId getId() { return id; }
    public String getName() { return name; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public boolean isActive() { return active; }
}
