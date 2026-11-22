package za.co.taxirankmanager.queue.domain;

import za.co.taxirankmanager.fleet.domain.TaxiId;
import za.co.taxirankmanager.shared.domain.DomainException;

public class DuplicateQueueEntryException extends DomainException {

    public DuplicateQueueEntryException(TaxiId taxiId) {
        super("Taxi " + taxiId + " is already in this queue");
    }
}
