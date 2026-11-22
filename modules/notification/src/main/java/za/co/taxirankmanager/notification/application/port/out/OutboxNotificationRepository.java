package za.co.taxirankmanager.notification.application.port.out;

import za.co.taxirankmanager.notification.domain.OutboxNotification;

import java.util.List;

public interface OutboxNotificationRepository {

    void save(OutboxNotification notification);

    void saveAll(List<OutboxNotification> notifications);

    List<OutboxNotification> findPending(int limit);
}
