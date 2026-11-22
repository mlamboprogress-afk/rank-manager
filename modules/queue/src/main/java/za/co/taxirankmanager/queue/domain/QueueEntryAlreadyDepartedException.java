package za.co.taxirankmanager.queue.domain;

import za.co.taxirankmanager.shared.domain.DomainException;

public class QueueEntryAlreadyDepartedException extends DomainException {

    public QueueEntryAlreadyDepartedException(QueueEntryId entryId) {
        super("Queue entry " + entryId + " has already departed");
    }
}
