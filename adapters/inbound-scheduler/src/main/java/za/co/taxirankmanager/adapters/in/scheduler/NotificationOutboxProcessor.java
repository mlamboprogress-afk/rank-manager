package za.co.taxirankmanager.adapters.in.scheduler;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

/**
 * Polls the notification outbox and dispatches pending SMS messages.
 * Runs every 30 seconds. The actual dispatch logic is wired via the application ports
 * once the full service layer is in place.
 */
@ApplicationScoped
public class NotificationOutboxProcessor {

    private static final Logger LOG = Logger.getLogger(NotificationOutboxProcessor.class);

    @Scheduled(every = "30s", delayed = "10s")
    void processPending() {
        LOG.debug("Outbox processor triggered — pending dispatch not yet wired");
    }
}
