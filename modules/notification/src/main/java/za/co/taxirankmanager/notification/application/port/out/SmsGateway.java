package za.co.taxirankmanager.notification.application.port.out;

import za.co.taxirankmanager.notification.domain.OutboxNotification;

public interface SmsGateway {

    /**
     * Sends the SMS for the given notification.
     * Returns the provider's message reference on success.
     * Throws SmsDeliveryException on a non-retriable failure.
     */
    String send(OutboxNotification notification);
}
