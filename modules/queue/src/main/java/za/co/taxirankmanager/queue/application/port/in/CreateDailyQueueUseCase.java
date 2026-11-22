package za.co.taxirankmanager.queue.application.port.in;

import za.co.taxirankmanager.queue.application.command.CreateDailyQueueCommand;
import za.co.taxirankmanager.queue.domain.DailyQueueId;

public interface CreateDailyQueueUseCase {

    DailyQueueId createQueue(CreateDailyQueueCommand command);
}
