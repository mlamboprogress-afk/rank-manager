package za.co.taxirankmanager.adapters.out.postgres.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import za.co.taxirankmanager.adapters.out.postgres.entity.TaxiJpaEntity;
import za.co.taxirankmanager.fleet.application.port.out.TaxiRepository;
import za.co.taxirankmanager.fleet.domain.Taxi;
import za.co.taxirankmanager.fleet.domain.TaxiId;
import za.co.taxirankmanager.fleet.domain.VehicleRegistration;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class JpaTaxiRepository implements TaxiRepository {

    @Inject
    EntityManager em;

    @Override
    public void save(Taxi taxi) {
        TaxiJpaEntity existing = em.find(TaxiJpaEntity.class, taxi.getId().value());
        if (existing == null) {
            TaxiJpaEntity entity = new TaxiJpaEntity();
            entity.setId(taxi.getId().value());
            entity.setRegistrationDisplay(taxi.getRegistration().getDisplayValue());
            entity.setRegistrationNormalised(taxi.getRegistration().getNormalisedValue());
            entity.setActive(taxi.isActive());
            entity.setVersion(0L);
            em.persist(entity);
        } else {
            existing.setActive(taxi.isActive());
        }
    }

    @Override
    public Optional<Taxi> findById(TaxiId id) {
        TaxiJpaEntity entity = em.find(TaxiJpaEntity.class, id.value());
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public Optional<Taxi> findByNormalisedRegistration(String normalisedRegistration) {
        return em.createQuery(
                "SELECT t FROM TaxiJpaEntity t WHERE t.registrationNormalised = :reg",
                TaxiJpaEntity.class)
                .setParameter("reg", normalisedRegistration)
                .getResultStream()
                .findFirst()
                .map(this::toDomain);
    }

    @Override
    public boolean existsByNormalisedRegistration(String normalisedRegistration) {
        Long count = em.createQuery(
                "SELECT COUNT(t) FROM TaxiJpaEntity t WHERE t.registrationNormalised = :reg",
                Long.class)
                .setParameter("reg", normalisedRegistration)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public List<Taxi> findAllActive() {
        return em.createQuery("SELECT t FROM TaxiJpaEntity t WHERE t.active = true ORDER BY t.registrationDisplay", TaxiJpaEntity.class)
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private Taxi toDomain(TaxiJpaEntity e) {
        return Taxi.reconstitute(
                TaxiId.of(e.getId()),
                VehicleRegistration.of(e.getRegistrationDisplay()),
                e.isActive(),
                e.getVersion());
    }
}
