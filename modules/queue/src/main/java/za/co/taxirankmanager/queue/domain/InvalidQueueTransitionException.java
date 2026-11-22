package za.co.taxirankmanager.queue.domain;

import za.co.taxirankmanager.shared.domain.DomainException;

public class InvalidQueueTransitionException extends DomainException {

    public InvalidQueueTransitionException(String message) {
        super(message);
    }
}
