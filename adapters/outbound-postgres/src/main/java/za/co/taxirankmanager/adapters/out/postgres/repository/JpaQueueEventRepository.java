package za.co.taxirankmanager.adapters.out.postgres.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import za.co.taxirankmanager.adapters.out.postgres.entity.QueueEventJpaEntity;
import za.co.taxirankmanager.identity.domain.UserId;
import za.co.taxirankmanager.queue.application.port.out.QueueEventRepository;
import za.co.taxirankmanager.queue.domain.DailyQueueId;
import za.co.taxirankmanager.queue.domain.QueueEntry;
import za.co.taxirankmanager.queue.domain.QueueEntryId;
import za.co.taxirankmanager.queue.domain.QueueEntryStatus;
import za.co.taxirankmanager.queue.domain.QueueEvent;
import za.co.taxirankmanager.queue.domain.QueueEventId;
import za.co.taxirankmanager.queue.domain.QueueEventType;

import java.util.List;

@ApplicationScoped
public class JpaQueueEventRepository implements QueueEventRepository {

    @Inject
    EntityManager em;

    @Override
    public void saveAll(List<QueueEvent> events) {
        for (QueueEvent event : events) {
            QueueEventJpaEntity entity = new QueueEventJpaEntity();
            entity.setId(event.id().value());
            entity.setQueueId(event.queueId().value());
            entity.setEventType(event.eventType().name());
            entity.setActorId(event.actorUserId() != null ? event.actorUserId().value() : null);
            entity.setOccurredAt(event.occurredAt());
            entity.setPayload(buildPayload(event));
            em.persist(entity);
        }
    }

    @Override
    public List<QueueEvent> findByQueueId(DailyQueueId queueId) {
        return em.createQuery(
                "SELECT e FROM QueueEventJpaEntity e WHERE e.queueId = :queueId ORDER BY e.occurredAt ASC",
                QueueEventJpaEntity.class)
                .setParameter("queueId", queueId.value())
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private String buildPayload(QueueEvent event) {
        StringBuilder sb = new StringBuilder("{");
        if (event.entryId() != null) {
            sb.append("\"entryId\":\"").append(event.entryId().value()).append("\",");
        }
        if (event.previousStatus() != null) {
            sb.append("\"previousStatus\":\"").append(event.previousStatus()).append("\",");
        }
        if (event.newStatus() != null) {
            sb.append("\"newStatus\":\"").append(event.newStatus()).append("\",");
        }
        if (event.previousPosition() != null) {
            sb.append("\"previousPosition\":").append(event.previousPosition()).append(",");
        }
        if (event.newPosition() != null) {
            sb.append("\"newPosition\":").append(event.newPosition()).append(",");
        }
        if (event.reason() != null) {
            sb.append("\"reason\":\"").append(event.reason().replace("\"", "\\\"")).append("\",");
        }
        if (sb.charAt(sb.length() - 1) == ',') {
            sb.setLength(sb.length() - 1);
        }
        sb.append("}");
        return sb.toString();
    }

    private QueueEvent toDomain(QueueEventJpaEntity e) {
        return new QueueEvent(
                new QueueEventId(e.getId()),
                DailyQueueId.of(e.getQueueId()),
                null,
                QueueEventType.valueOf(e.getEventType()),
                null, null, null, null, null,
                e.getActorId() != null ? UserId.of(e.getActorId()) : null,
                e.getOccurredAt());
    }
}
