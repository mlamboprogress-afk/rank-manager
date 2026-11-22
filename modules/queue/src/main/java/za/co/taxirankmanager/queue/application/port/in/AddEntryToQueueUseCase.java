package za.co.taxirankmanager.queue.application.port.in;

import za.co.taxirankmanager.queue.application.command.AddEntryToQueueCommand;
import za.co.taxirankmanager.queue.domain.QueueEntryId;

public interface AddEntryToQueueUseCase {

    QueueEntryId addEntry(AddEntryToQueueCommand command);
}
