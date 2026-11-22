package za.co.taxirankmanager.adapters.out.postgres.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import za.co.taxirankmanager.adapters.out.postgres.entity.RankRouteJpaEntity;
import za.co.taxirankmanager.rank.domain.RankId;
import za.co.taxirankmanager.route.application.port.out.RankRouteRepository;
import za.co.taxirankmanager.route.domain.RankRoute;
import za.co.taxirankmanager.route.domain.RankRouteId;
import za.co.taxirankmanager.route.domain.RouteId;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class JpaRankRouteRepository implements RankRouteRepository {

    @Inject
    EntityManager em;

    @Override
    public void save(RankRoute rankRoute) {
        RankRouteJpaEntity existing = em.find(RankRouteJpaEntity.class, rankRoute.getId().value());
        if (existing == null) {
            RankRouteJpaEntity entity = new RankRouteJpaEntity();
            entity.setId(rankRoute.getId().value());
            entity.setRankId(rankRoute.getRankId().value());
            entity.setRouteId(rankRoute.getRouteId().value());
            entity.setActive(rankRoute.isActive());
            em.persist(entity);
        } else {
            existing.setActive(rankRoute.isActive());
        }
    }

    @Override
    public Optional<RankRoute> findById(RankRouteId id) {
        RankRouteJpaEntity entity = em.find(RankRouteJpaEntity.class, id.value());
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public List<RankRoute> findByRankId(RankId rankId) {
        return em.createQuery(
                "SELECT r FROM RankRouteJpaEntity r WHERE r.rankId = :rankId AND r.active = true",
                RankRouteJpaEntity.class)
                .setParameter("rankId", rankId.value())
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<RankRoute> findAllActive() {
        return em.createQuery("SELECT r FROM RankRouteJpaEntity r WHERE r.active = true", RankRouteJpaEntity.class)
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private RankRoute toDomain(RankRouteJpaEntity e) {
        return RankRoute.reconstitute(
                RankRouteId.of(e.getId()),
                RankId.of(e.getRankId()),
                RouteId.of(e.getRouteId()),
                e.isActive());
    }
}
