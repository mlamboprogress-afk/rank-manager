package za.co.taxirankmanager.adapters.out.sms;

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import za.co.taxirankmanager.notification.application.port.out.SmsGateway;
import za.co.taxirankmanager.notification.domain.OutboxNotification;

import java.util.UUID;

/**
 * Stub SMS gateway — logs the SMS instead of sending it.
 * Replace with a real provider implementation before go-live.
 */
@ApplicationScoped
public class StubSmsGateway implements SmsGateway {

    private static final Logger LOG = Logger.getLogger(StubSmsGateway.class);

    @Override
    public String send(OutboxNotification notification) {
        LOG.infof("[STUB SMS] to=%s event=%s message=%s",
                notification.getMobileNumber(),
                notification.getEvent(),
                notification.getMessageText());
        return "STUB-" + UUID.randomUUID();
    }
}
