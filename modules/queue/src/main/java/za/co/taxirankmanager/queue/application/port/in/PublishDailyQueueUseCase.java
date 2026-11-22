package za.co.taxirankmanager.queue.application.port.in;

import za.co.taxirankmanager.queue.application.command.PublishDailyQueueCommand;

public interface PublishDailyQueueUseCase {

    void publishQueue(PublishDailyQueueCommand command);
}
