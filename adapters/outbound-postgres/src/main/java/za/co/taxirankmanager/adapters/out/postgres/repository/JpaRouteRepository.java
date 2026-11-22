package za.co.taxirankmanager.adapters.out.postgres.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import za.co.taxirankmanager.adapters.out.postgres.entity.RouteJpaEntity;
import za.co.taxirankmanager.route.application.port.out.RouteRepository;
import za.co.taxirankmanager.route.domain.Route;
import za.co.taxirankmanager.route.domain.RouteId;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class JpaRouteRepository implements RouteRepository {

    @Inject
    EntityManager em;

    @Override
    public void save(Route route) {
        RouteJpaEntity existing = em.find(RouteJpaEntity.class, route.getId().value());
        if (existing == null) {
            RouteJpaEntity entity = new RouteJpaEntity();
            entity.setId(route.getId().value());
            entity.setName(route.getName());
            entity.setOrigin(route.getOrigin());
            entity.setDestination(route.getDestination());
            entity.setActive(route.isActive());
            em.persist(entity);
        } else {
            existing.setName(route.getName());
            existing.setOrigin(route.getOrigin());
            existing.setDestination(route.getDestination());
            existing.setActive(route.isActive());
        }
    }

    @Override
    public Optional<Route> findById(RouteId id) {
        RouteJpaEntity entity = em.find(RouteJpaEntity.class, id.value());
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public List<Route> findAllActive() {
        return em.createQuery("SELECT r FROM RouteJpaEntity r WHERE r.active = true ORDER BY r.name", RouteJpaEntity.class)
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private Route toDomain(RouteJpaEntity e) {
        return Route.reconstitute(RouteId.of(e.getId()), e.getName(), e.getOrigin(), e.getDestination(), e.isActive());
    }
}
