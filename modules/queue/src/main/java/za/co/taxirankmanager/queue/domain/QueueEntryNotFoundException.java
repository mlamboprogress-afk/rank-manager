package za.co.taxirankmanager.queue.domain;

import za.co.taxirankmanager.shared.domain.DomainException;

public class QueueEntryNotFoundException extends DomainException {

    public QueueEntryNotFoundException(QueueEntryId entryId) {
        super("Queue entry not found: " + entryId);
    }
}
