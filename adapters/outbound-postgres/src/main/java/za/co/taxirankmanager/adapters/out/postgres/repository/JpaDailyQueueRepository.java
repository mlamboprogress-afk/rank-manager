package za.co.taxirankmanager.adapters.out.postgres.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import za.co.taxirankmanager.adapters.out.postgres.entity.DailyQueueJpaEntity;
import za.co.taxirankmanager.adapters.out.postgres.entity.QueueEntryJpaEntity;
import za.co.taxirankmanager.fleet.domain.TaxiId;
import za.co.taxirankmanager.queue.application.port.out.DailyQueueRepository;
import za.co.taxirankmanager.queue.application.port.out.QueueListRepository;
import za.co.taxirankmanager.queue.domain.DailyQueue;
import za.co.taxirankmanager.queue.domain.DailyQueueId;
import za.co.taxirankmanager.queue.domain.QueueEntry;
import za.co.taxirankmanager.queue.domain.QueueEntryId;
import za.co.taxirankmanager.queue.domain.QueueEntryStatus;
import za.co.taxirankmanager.queue.domain.QueueLifecycleStatus;
import za.co.taxirankmanager.route.domain.RankRouteId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class JpaDailyQueueRepository implements DailyQueueRepository, QueueListRepository {

    @Inject
    EntityManager em;

    @Override
    public void save(DailyQueue queue) {
        DailyQueueJpaEntity existing = em.find(DailyQueueJpaEntity.class, queue.getId().value());
        Instant now = Instant.now();

        if (existing == null) {
            DailyQueueJpaEntity entity = new DailyQueueJpaEntity();
            entity.setId(queue.getId().value());
            entity.setRankRouteId(queue.getRankRouteId().value());
            entity.setServiceDate(queue.getServiceDate());
            entity.setLifecycleStatus(queue.getLifecycleStatus().name());
            entity.setCreatedBy(null);
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
            entity.setVersion(0L);

            for (QueueEntry entry : queue.getEntries()) {
                QueueEntryJpaEntity entryEntity = buildEntryEntity(entry, entity, now);
                entity.getEntries().add(entryEntity);
            }
            em.persist(entity);
        } else {
            existing.setLifecycleStatus(queue.getLifecycleStatus().name());
            existing.setUpdatedAt(now);
            syncEntries(existing, queue.getEntries(), now);
        }
    }

    @Override
    public Optional<DailyQueue> findById(DailyQueueId id) {
        DailyQueueJpaEntity entity = em.find(DailyQueueJpaEntity.class, id.value());
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public Optional<DailyQueue> findByRankRouteAndDate(RankRouteId rankRouteId, LocalDate serviceDate) {
        return em.createQuery(
                "SELECT q FROM DailyQueueJpaEntity q WHERE q.rankRouteId = :rrId AND q.serviceDate = :date " +
                "AND q.lifecycleStatus <> 'CANCELLED'",
                DailyQueueJpaEntity.class)
                .setParameter("rrId", rankRouteId.value())
                .setParameter("date", serviceDate)
                .getResultStream()
                .findFirst()
                .map(this::toDomain);
    }

    @Override
    public boolean existsByRankRouteAndDate(RankRouteId rankRouteId, LocalDate serviceDate) {
        Long count = em.createQuery(
                "SELECT COUNT(q) FROM DailyQueueJpaEntity q WHERE q.rankRouteId = :rrId AND q.serviceDate = :date " +
                "AND q.lifecycleStatus <> 'CANCELLED'",
                Long.class)
                .setParameter("rrId", rankRouteId.value())
                .setParameter("date", serviceDate)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public List<DailyQueue> findByRankRoute(RankRouteId rankRouteId) {
        return em.createQuery(
                "SELECT q FROM DailyQueueJpaEntity q WHERE q.rankRouteId = :rrId ORDER BY q.serviceDate DESC",
                DailyQueueJpaEntity.class)
                .setParameter("rrId", rankRouteId.value())
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void syncEntries(DailyQueueJpaEntity queueEntity, List<QueueEntry> domainEntries, Instant now) {
        Set<UUID> domainIds = domainEntries.stream()
                .map(e -> e.getId().value())
                .collect(Collectors.toSet());

        queueEntity.getEntries().removeIf(e -> !domainIds.contains(e.getId()));

        Map<UUID, QueueEntryJpaEntity> existingById = queueEntity.getEntries().stream()
                .collect(Collectors.toMap(QueueEntryJpaEntity::getId, e -> e));

        for (QueueEntry entry : domainEntries) {
            UUID entryId = entry.getId().value();
            QueueEntryJpaEntity entryEntity = existingById.get(entryId);
            if (entryEntity == null) {
                entryEntity = buildEntryEntity(entry, queueEntity, now);
                queueEntity.getEntries().add(entryEntity);
            } else {
                entryEntity.setPosition(entry.getPosition());
                entryEntity.setStatus(entry.getStatus().name());
                entryEntity.setUpdatedAt(now);
            }
        }
    }

    private QueueEntryJpaEntity buildEntryEntity(QueueEntry entry, DailyQueueJpaEntity queueEntity, Instant now) {
        QueueEntryJpaEntity e = new QueueEntryJpaEntity();
        e.setId(entry.getId().value());
        e.setQueue(queueEntity);
        e.setTaxiId(entry.getTaxiId().value());
        e.setPosition(entry.getPosition());
        e.setStatus(entry.getStatus().name());
        e.setCreatedAt(now);
        e.setUpdatedAt(now);
        e.setVersion(0L);
        return e;
    }

    private DailyQueue toDomain(DailyQueueJpaEntity e) {
        List<QueueEntry> entries = e.getEntries().stream()
                .map(entry -> QueueEntry.reconstitute(
                        QueueEntryId.of(entry.getId()),
                        TaxiId.of(entry.getTaxiId()),
                        entry.getPosition(),
                        QueueEntryStatus.valueOf(entry.getStatus()),
                        entry.getVersion()))
                .toList();

        return DailyQueue.reconstitute(
                DailyQueueId.of(e.getId()),
                RankRouteId.of(e.getRankRouteId()),
                e.getServiceDate(),
                QueueLifecycleStatus.valueOf(e.getLifecycleStatus()),
                entries,
                e.getVersion());
    }
}
