package za.co.taxirankmanager.notification.domain;

import za.co.taxirankmanager.fleet.domain.DriverId;

import java.time.Instant;
import java.util.Objects;

/**
 * A pending notification in the transactional outbox.
 * Written atomically with the queue operation. Processed asynchronously by the scheduler.
 */
public class OutboxNotification {

    private final NotificationId id;
    private final DriverId driverId;
    private final String mobileNumber;
    private final NotificationEvent event;
    private final String messageText;
    private DeliveryStatus deliveryStatus;
    private final Instant createdAt;
    private Instant processedAt;
    private String providerReference;

    private OutboxNotification(NotificationId id, DriverId driverId, String mobileNumber,
                                NotificationEvent event, String messageText,
                                DeliveryStatus deliveryStatus, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.driverId = Objects.requireNonNull(driverId);
        this.mobileNumber = Objects.requireNonNull(mobileNumber);
        this.event = Objects.requireNonNull(event);
        this.messageText = Objects.requireNonNull(messageText);
        this.deliveryStatus = deliveryStatus;
        this.createdAt = createdAt;
    }

    public static OutboxNotification create(DriverId driverId, String mobileNumber,
                                            NotificationEvent event, String messageText, Instant now) {
        return new OutboxNotification(
                NotificationId.generate(), driverId, mobileNumber,
                event, messageText, DeliveryStatus.PENDING, now);
    }

    public static OutboxNotification reconstitute(NotificationId id, DriverId driverId, String mobileNumber,
                                                  NotificationEvent event, String messageText,
                                                  DeliveryStatus deliveryStatus, Instant createdAt,
                                                  Instant processedAt, String providerReference) {
        OutboxNotification n = new OutboxNotification(id, driverId, mobileNumber, event, messageText, deliveryStatus, createdAt);
        n.processedAt = processedAt;
        n.providerReference = providerReference;
        return n;
    }

    public void markSent(String providerReference, Instant now) {
        this.deliveryStatus = DeliveryStatus.SENT;
        this.providerReference = providerReference;
        this.processedAt = now;
    }

    public void markFailed(Instant now) {
        this.deliveryStatus = DeliveryStatus.FAILED;
        this.processedAt = now;
    }

    public NotificationId getId() { return id; }
    public DriverId getDriverId() { return driverId; }
    public String getMobileNumber() { return mobileNumber; }
    public NotificationEvent getEvent() { return event; }
    public String getMessageText() { return messageText; }
    public DeliveryStatus getDeliveryStatus() { return deliveryStatus; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getProcessedAt() { return processedAt; }
    public String getProviderReference() { return providerReference; }
}
