package za.co.taxirankmanager.adapters.out.postgres.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import za.co.taxirankmanager.adapters.out.postgres.entity.NotificationOutboxJpaEntity;
import za.co.taxirankmanager.fleet.domain.DriverId;
import za.co.taxirankmanager.notification.application.port.out.OutboxNotificationRepository;
import za.co.taxirankmanager.notification.domain.DeliveryStatus;
import za.co.taxirankmanager.notification.domain.NotificationEvent;
import za.co.taxirankmanager.notification.domain.NotificationId;
import za.co.taxirankmanager.notification.domain.OutboxNotification;

import java.util.List;

@ApplicationScoped
public class JpaNotificationOutboxRepository implements OutboxNotificationRepository {

    @Inject
    EntityManager em;

    @Override
    public void save(OutboxNotification notification) {
        NotificationOutboxJpaEntity existing = em.find(NotificationOutboxJpaEntity.class, notification.getId().value());
        if (existing == null) {
            NotificationOutboxJpaEntity entity = toEntity(notification);
            em.persist(entity);
        } else {
            existing.setDeliveryStatus(notification.getDeliveryStatus().name());
            existing.setProcessedAt(notification.getProcessedAt());
            existing.setProviderReference(notification.getProviderReference());
        }
    }

    @Override
    public void saveAll(List<OutboxNotification> notifications) {
        notifications.forEach(this::save);
    }

    @Override
    public List<OutboxNotification> findPending(int limit) {
        return em.createQuery(
                "SELECT n FROM NotificationOutboxJpaEntity n WHERE n.deliveryStatus = 'PENDING' ORDER BY n.createdAt ASC",
                NotificationOutboxJpaEntity.class)
                .setMaxResults(limit)
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private NotificationOutboxJpaEntity toEntity(OutboxNotification n) {
        NotificationOutboxJpaEntity entity = new NotificationOutboxJpaEntity();
        entity.setId(n.getId().value());
        entity.setDriverId(n.getDriverId().value());
        entity.setMobileNumber(n.getMobileNumber());
        entity.setEventType(n.getEvent().name());
        entity.setMessageText(n.getMessageText());
        entity.setDeliveryStatus(n.getDeliveryStatus().name());
        entity.setCreatedAt(n.getCreatedAt());
        entity.setProcessedAt(n.getProcessedAt());
        entity.setProviderReference(n.getProviderReference());
        return entity;
    }

    private OutboxNotification toDomain(NotificationOutboxJpaEntity e) {
        return OutboxNotification.reconstitute(
                NotificationId.of(e.getId()),
                DriverId.of(e.getDriverId()),
                e.getMobileNumber(),
                NotificationEvent.valueOf(e.getEventType()),
                e.getMessageText(),
                DeliveryStatus.valueOf(e.getDeliveryStatus()),
                e.getCreatedAt(),
                e.getProcessedAt(),
                e.getProviderReference());
    }
}
