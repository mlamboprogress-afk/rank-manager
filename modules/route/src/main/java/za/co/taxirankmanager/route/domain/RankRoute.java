package za.co.taxirankmanager.route.domain;

import za.co.taxirankmanager.rank.domain.RankId;

import java.util.Objects;

/**
 * A route offered at a specific taxi rank.
 * This is the operational unit that daily queues are created against.
 */
public class RankRoute {

    private final RankRouteId id;
    private final RankId rankId;
    private final RouteId routeId;
    private boolean active;

    private RankRoute(RankRouteId id, RankId rankId, RouteId routeId, boolean active) {
        this.id = Objects.requireNonNull(id);
        this.rankId = Objects.requireNonNull(rankId);
        this.routeId = Objects.requireNonNull(routeId);
        this.active = active;
    }

    public static RankRoute create(RankRouteId id, RankId rankId, RouteId routeId) {
        return new RankRoute(id, rankId, routeId, true);
    }

    public static RankRoute reconstitute(RankRouteId id, RankId rankId, RouteId routeId, boolean active) {
        return new RankRoute(id, rankId, routeId, active);
    }

    public void deactivate() {
        this.active = false;
    }

    public RankRouteId getId() { return id; }
    public RankId getRankId() { return rankId; }
    public RouteId getRouteId() { return routeId; }
    public boolean isActive() { return active; }
}
