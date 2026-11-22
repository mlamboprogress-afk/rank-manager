package za.co.taxirankmanager.adapters.out.postgres.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import za.co.taxirankmanager.adapters.out.postgres.entity.DriverJpaEntity;
import za.co.taxirankmanager.fleet.application.port.out.DriverRepository;
import za.co.taxirankmanager.fleet.domain.Driver;
import za.co.taxirankmanager.fleet.domain.DriverId;
import za.co.taxirankmanager.fleet.domain.SouthAfricanIdNumber;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class JpaDriverRepository implements DriverRepository {

    @Inject
    EntityManager em;

    @Override
    public void save(Driver driver) {
        DriverJpaEntity existing = em.find(DriverJpaEntity.class, driver.getId().value());
        if (existing == null) {
            DriverJpaEntity entity = new DriverJpaEntity();
            entity.setId(driver.getId().value());
            entity.setIdNumberHash(driver.getIdNumber().hash());
            entity.setIdNumberEnc(driver.getIdNumber().getValue().getBytes(StandardCharsets.UTF_8));
            entity.setFirstName(driver.getFirstName());
            entity.setLastName(driver.getLastName());
            entity.setMobileNumber(driver.getMobileNumber());
            entity.setActive(driver.isActive());
            entity.setVersion(0L);
            em.persist(entity);
        } else {
            existing.setFirstName(driver.getFirstName());
            existing.setLastName(driver.getLastName());
            existing.setMobileNumber(driver.getMobileNumber());
            existing.setActive(driver.isActive());
        }
    }

    @Override
    public Optional<Driver> findById(DriverId id) {
        DriverJpaEntity entity = em.find(DriverJpaEntity.class, id.value());
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public Optional<Driver> findByIdNumberHash(String idNumberHash) {
        return em.createQuery(
                "SELECT d FROM DriverJpaEntity d WHERE d.idNumberHash = :hash",
                DriverJpaEntity.class)
                .setParameter("hash", idNumberHash)
                .getResultStream()
                .findFirst()
                .map(this::toDomain);
    }

    @Override
    public List<Driver> findAllActive() {
        return em.createQuery(
                "SELECT d FROM DriverJpaEntity d WHERE d.active = true ORDER BY d.lastName, d.firstName",
                DriverJpaEntity.class)
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private Driver toDomain(DriverJpaEntity e) {
        String rawIdNumber = new String(e.getIdNumberEnc(), StandardCharsets.UTF_8);
        SouthAfricanIdNumber idNumber = SouthAfricanIdNumber.of(rawIdNumber);
        return Driver.reconstitute(
                DriverId.of(e.getId()),
                idNumber,
                e.getFirstName(),
                e.getLastName(),
                e.getMobileNumber(),
                e.isActive(),
                e.getVersion());
    }
}
