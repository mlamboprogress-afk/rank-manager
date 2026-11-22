package za.co.taxirankmanager.adapters.out.postgres.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import za.co.taxirankmanager.adapters.out.postgres.entity.RankJpaEntity;
import za.co.taxirankmanager.rank.application.port.out.RankRepository;
import za.co.taxirankmanager.rank.domain.Rank;
import za.co.taxirankmanager.rank.domain.RankId;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class JpaRankRepository implements RankRepository {

    @Inject
    EntityManager em;

    @Override
    public void save(Rank rank) {
        RankJpaEntity existing = em.find(RankJpaEntity.class, rank.getId().value());
        if (existing == null) {
            RankJpaEntity entity = new RankJpaEntity();
            entity.setId(rank.getId().value());
            entity.setName(rank.getName());
            entity.setLocationDescription(rank.getLocationDescription());
            entity.setActive(rank.isActive());
            em.persist(entity);
        } else {
            existing.setName(rank.getName());
            existing.setLocationDescription(rank.getLocationDescription());
            existing.setActive(rank.isActive());
        }
    }

    @Override
    public Optional<Rank> findById(RankId id) {
        RankJpaEntity entity = em.find(RankJpaEntity.class, id.value());
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public List<Rank> findAllActive() {
        return em.createQuery("SELECT r FROM RankJpaEntity r WHERE r.active = true ORDER BY r.name", RankJpaEntity.class)
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private Rank toDomain(RankJpaEntity e) {
        return Rank.reconstitute(RankId.of(e.getId()), e.getName(), e.getLocationDescription(), e.isActive());
    }
}
