package za.co.taxirankmanager.queue.application.port.out;

import za.co.taxirankmanager.queue.domain.DailyQueueId;
import za.co.taxirankmanager.queue.domain.QueueEvent;

import java.util.List;

public interface QueueEventRepository {

    void saveAll(List<QueueEvent> events);

    List<QueueEvent> findByQueueId(DailyQueueId queueId);
}
