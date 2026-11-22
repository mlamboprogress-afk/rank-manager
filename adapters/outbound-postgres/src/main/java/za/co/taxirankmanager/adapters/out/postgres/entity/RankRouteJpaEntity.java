package za.co.taxirankmanager.adapters.out.postgres.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "rank_route")
public class RankRouteJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "rank_id", nullable = false)
    private UUID rankId;

    @Column(name = "route_id", nullable = false)
    private UUID routeId;

    @Column(name = "active", nullable = false)
    private boolean active;

    public RankRouteJpaEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getRankId() { return rankId; }
    public void setRankId(UUID rankId) { this.rankId = rankId; }
    public UUID getRouteId() { return routeId; }
    public void setRouteId(UUID routeId) { this.routeId = routeId; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
