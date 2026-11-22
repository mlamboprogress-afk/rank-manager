package za.co.taxirankmanager.queue.domain;

import org.junit.jupiter.api.Test;
import za.co.taxirankmanager.fleet.domain.TaxiId;
import za.co.taxirankmanager.identity.domain.UserId;
import za.co.taxirankmanager.route.domain.RankRouteId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DailyQueueTest {

    private final RankRouteId rankRouteId = RankRouteId.generate();
    private final LocalDate today = LocalDate.of(2024, 11, 22);
    private final UserId admin = UserId.of(UUID.randomUUID());
    private final Instant now = Instant.parse("2024-11-22T07:00:00Z");

    private DailyQueue freshPublishedQueue(TaxiId... taxis) {
        DailyQueue queue = DailyQueue.create(DailyQueueId.generate(), rankRouteId, today, admin, now);
        for (TaxiId taxi : taxis) {
            queue.addEntry(QueueEntryId.generate(), taxi, admin, now);
        }
        queue.drainPendingEvents();
        queue.publish(admin, now);
        queue.drainPendingEvents();
        return queue;
    }

    // -------------------------------------------------------------------------
    // publish
    // -------------------------------------------------------------------------

    @Test
    void publish_draftQueueWithEntries_succeeds() {
        DailyQueue queue = DailyQueue.create(DailyQueueId.generate(), rankRouteId, today, admin, now);
        queue.addEntry(QueueEntryId.generate(), TaxiId.generate(), admin, now);
        queue.drainPendingEvents();

        assertDoesNotThrow(() -> queue.publish(admin, now));
        assertEquals(QueueLifecycleStatus.PUBLISHED, queue.getLifecycleStatus());
    }

    @Test
    void publish_emptyQueue_throwsInvalidTransition() {
        DailyQueue queue = DailyQueue.create(DailyQueueId.generate(), rankRouteId, today, admin, now);
        queue.drainPendingEvents();

        assertThrows(InvalidQueueTransitionException.class, () -> queue.publish(admin, now));
    }

    @Test
    void publish_alreadyPublished_throwsInvalidTransition() {
        TaxiId taxi = TaxiId.generate();
        DailyQueue queue = freshPublishedQueue(taxi);

        assertThrows(InvalidQueueTransitionException.class, () -> queue.publish(admin, now));
    }

    // -------------------------------------------------------------------------
    // addEntry
    // -------------------------------------------------------------------------

    @Test
    void addEntry_duplicateTaxi_throwsDuplicateEntry() {
        TaxiId taxi = TaxiId.generate();
        DailyQueue queue = DailyQueue.create(DailyQueueId.generate(), rankRouteId, today, admin, now);
        queue.addEntry(QueueEntryId.generate(), taxi, admin, now);

        assertThrows(DuplicateQueueEntryException.class,
                () -> queue.addEntry(QueueEntryId.generate(), taxi, admin, now));
    }

    @Test
    void addEntry_toPublishedQueue_throwsInvalidTransition() {
        TaxiId taxi1 = TaxiId.generate();
        DailyQueue queue = freshPublishedQueue(taxi1);

        assertThrows(InvalidQueueTransitionException.class,
                () -> queue.addEntry(QueueEntryId.generate(), TaxiId.generate(), admin, now));
    }

    // -------------------------------------------------------------------------
    // depart
    // -------------------------------------------------------------------------

    @Test
    void depart_waitingEntry_changesStatusToDeparted() {
        TaxiId taxi = TaxiId.generate();
        DailyQueue queue = freshPublishedQueue(taxi);
        QueueEntryId entryId = queue.getEntries().get(0).getId();

        queue.depart(entryId, admin, now);

        assertEquals(QueueEntryStatus.DEPARTED, queue.getEntries().get(0).getStatus());
    }

    @Test
    void depart_alreadyDeparted_throwsAlreadyDeparted() {
        TaxiId taxi = TaxiId.generate();
        DailyQueue queue = freshPublishedQueue(taxi);
        QueueEntryId entryId = queue.getEntries().get(0).getId();
        queue.depart(entryId, admin, now);
        queue.drainPendingEvents();

        assertThrows(QueueEntryAlreadyDepartedException.class,
                () -> queue.depart(entryId, admin, now));
    }

    // -------------------------------------------------------------------------
    // skip — ordering invariants
    // -------------------------------------------------------------------------

    @Test
    void skip_firstTaxi_movesItToEndOfWaiting() {
        TaxiId a = TaxiId.generate();
        TaxiId b = TaxiId.generate();
        TaxiId c = TaxiId.generate();
        DailyQueue queue = freshPublishedQueue(a, b, c);

        QueueEntryId entryA = queue.getEntries().stream()
                .filter(e -> e.getTaxiId().equals(a)).findFirst().orElseThrow().getId();

        queue.skip(entryA, admin, "unavailable", now);

        List<QueueEntry> entries = queue.getEntries();
        assertEquals(b, entries.get(0).getTaxiId());
        assertEquals(c, entries.get(1).getTaxiId());
        assertEquals(a, entries.get(2).getTaxiId());
        assertEquals(QueueEntryStatus.SKIPPED, entries.get(2).getStatus());
    }

    @Test
    void skip_otherTaxisRetainRelativeOrder() {
        TaxiId a = TaxiId.generate();
        TaxiId b = TaxiId.generate();
        TaxiId c = TaxiId.generate();
        TaxiId d = TaxiId.generate();
        DailyQueue queue = freshPublishedQueue(a, b, c, d);

        QueueEntryId entryB = queue.getEntries().stream()
                .filter(e -> e.getTaxiId().equals(b)).findFirst().orElseThrow().getId();

        queue.skip(entryB, admin, "not ready", now);

        List<QueueEntry> entries = queue.getEntries();
        assertEquals(a, entries.get(0).getTaxiId());
        assertEquals(c, entries.get(1).getTaxiId());
        assertEquals(d, entries.get(2).getTaxiId());
        assertEquals(b, entries.get(3).getTaxiId());
    }

    @Test
    void skip_departedEntry_throwsInvalidTransition() {
        TaxiId taxi = TaxiId.generate();
        DailyQueue queue = freshPublishedQueue(taxi);
        QueueEntryId entryId = queue.getEntries().get(0).getId();
        queue.depart(entryId, admin, now);
        queue.drainPendingEvents();

        assertThrows(InvalidQueueTransitionException.class,
                () -> queue.skip(entryId, admin, "reason", now));
    }

    @Test
    void skip_positionsReassignedCorrectly() {
        TaxiId a = TaxiId.generate();
        TaxiId b = TaxiId.generate();
        TaxiId c = TaxiId.generate();
        DailyQueue queue = freshPublishedQueue(a, b, c);

        QueueEntryId entryA = queue.getEntries().stream()
                .filter(e -> e.getTaxiId().equals(a)).findFirst().orElseThrow().getId();
        queue.skip(entryA, admin, "reason", now);

        List<QueueEntry> entries = queue.getEntries();
        assertEquals(1, entries.get(0).getPosition());
        assertEquals(2, entries.get(1).getPosition());
        assertEquals(3, entries.get(2).getPosition());
    }

    // -------------------------------------------------------------------------
    // Events
    // -------------------------------------------------------------------------

    @Test
    void create_producesDailyQueueCreatedEvent() {
        DailyQueue queue = DailyQueue.create(DailyQueueId.generate(), rankRouteId, today, admin, now);
        List<QueueEvent> events = queue.drainPendingEvents();

        assertEquals(1, events.size());
        assertEquals(QueueEventType.DAILY_QUEUE_CREATED, events.get(0).eventType());
    }

    @Test
    void drainPendingEvents_clearsEvents() {
        DailyQueue queue = DailyQueue.create(DailyQueueId.generate(), rankRouteId, today, admin, now);
        queue.drainPendingEvents();

        assertTrue(queue.drainPendingEvents().isEmpty());
    }
}
