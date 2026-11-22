package za.co.taxirankmanager.queue.domain;

import za.co.taxirankmanager.fleet.domain.TaxiId;
import za.co.taxirankmanager.identity.domain.UserId;
import za.co.taxirankmanager.route.domain.RankRouteId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate root for a daily taxi queue at a specific rank-route.
 *
 * Invariants enforced here:
 * - A taxi may appear only once in a queue.
 * - Entries can only be added to a DRAFT queue.
 * - Only a DRAFT queue can be published.
 * - A DEPARTED entry cannot be skipped or departed again.
 * - After a skip, the skipped taxi moves to the back of all WAITING entries.
 * - Other WAITING taxis retain their relative order after a skip.
 *
 * The version field is used for optimistic locking — the application service
 * must pass the client's known version when loading and saving.
 */
public class DailyQueue {

    private final DailyQueueId id;
    private final RankRouteId rankRouteId;
    private final LocalDate serviceDate;
    private QueueLifecycleStatus lifecycleStatus;
    private final List<QueueEntry> entries;
    private long version;
    private final List<QueueEvent> pendingEvents;

    private DailyQueue(DailyQueueId id, RankRouteId rankRouteId, LocalDate serviceDate,
                       QueueLifecycleStatus lifecycleStatus, List<QueueEntry> entries, long version) {
        this.id = Objects.requireNonNull(id);
        this.rankRouteId = Objects.requireNonNull(rankRouteId);
        this.serviceDate = Objects.requireNonNull(serviceDate);
        this.lifecycleStatus = Objects.requireNonNull(lifecycleStatus);
        this.entries = new ArrayList<>(entries);
        this.version = version;
        this.pendingEvents = new ArrayList<>();
    }

    public static DailyQueue create(DailyQueueId id, RankRouteId rankRouteId,
                                    LocalDate serviceDate, UserId createdBy, Instant now) {
        DailyQueue queue = new DailyQueue(id, rankRouteId, serviceDate,
                QueueLifecycleStatus.DRAFT, List.of(), 0L);
        queue.pendingEvents.add(QueueEvent.queueCreated(id, createdBy, now));
        return queue;
    }

    public static DailyQueue reconstitute(DailyQueueId id, RankRouteId rankRouteId, LocalDate serviceDate,
                                          QueueLifecycleStatus lifecycleStatus, List<QueueEntry> entries,
                                          long version) {
        return new DailyQueue(id, rankRouteId, serviceDate, lifecycleStatus, entries, version);
    }

    // -------------------------------------------------------------------------
    // Queue building (DRAFT only)
    // -------------------------------------------------------------------------

    public void addEntry(QueueEntryId entryId, TaxiId taxiId, UserId actor, Instant now) {
        if (lifecycleStatus != QueueLifecycleStatus.DRAFT) {
            throw new InvalidQueueTransitionException(
                    "Entries can only be added to a DRAFT queue; current status: " + lifecycleStatus);
        }
        if (entries.stream().anyMatch(e -> e.getTaxiId().equals(taxiId))) {
            throw new DuplicateQueueEntryException(taxiId);
        }
        int position = entries.size() + 1;
        QueueEntry entry = QueueEntry.create(entryId, taxiId, position);
        entries.add(entry);
        pendingEvents.add(QueueEvent.taxiAdded(id, entryId, position, actor, now));
    }

    // -------------------------------------------------------------------------
    // Lifecycle transitions
    // -------------------------------------------------------------------------

    public void publish(UserId publishedBy, Instant now) {
        if (lifecycleStatus != QueueLifecycleStatus.DRAFT) {
            throw new InvalidQueueTransitionException(
                    "Only a DRAFT queue can be published; current status: " + lifecycleStatus);
        }
        if (entries.isEmpty()) {
            throw new InvalidQueueTransitionException("Cannot publish an empty queue");
        }
        this.lifecycleStatus = QueueLifecycleStatus.PUBLISHED;
        pendingEvents.add(QueueEvent.queuePublished(id, publishedBy, now));
    }

    public void cancel(String reason, UserId actor, Instant now) {
        if (lifecycleStatus == QueueLifecycleStatus.CANCELLED || lifecycleStatus == QueueLifecycleStatus.COMPLETED) {
            throw new InvalidQueueTransitionException(
                    "Cannot cancel a queue with status: " + lifecycleStatus);
        }
        this.lifecycleStatus = QueueLifecycleStatus.CANCELLED;
        pendingEvents.add(QueueEvent.queueCancelled(id, reason, actor, now));
    }

    // -------------------------------------------------------------------------
    // Queue operations (PUBLISHED only)
    // -------------------------------------------------------------------------

    public void depart(QueueEntryId entryId, UserId actor, Instant now) {
        requirePublished();
        QueueEntry entry = findEntry(entryId);
        if (entry.getStatus() == QueueEntryStatus.DEPARTED) {
            throw new QueueEntryAlreadyDepartedException(entryId);
        }
        int position = entry.getPosition();
        QueueEntryStatus prevStatus = entry.getStatus();
        entry.markDeparted();
        pendingEvents.add(QueueEvent.taxiDeparted(id, entryId, prevStatus, position, actor, now));
    }

    public void skip(QueueEntryId entryId, UserId actor, String reason, Instant now) {
        requirePublished();
        QueueEntry entry = findEntry(entryId);
        if (entry.getStatus() == QueueEntryStatus.DEPARTED) {
            throw new InvalidQueueTransitionException("Cannot skip a departed taxi");
        }

        int prevPosition = entry.getPosition();
        QueueEntryStatus prevStatus = entry.getStatus();

        // Remove from current list position, insert after the last WAITING entry
        entries.remove(entry);
        int lastWaitingIndex = findLastWaitingIndex();
        int insertIndex = lastWaitingIndex + 1;
        entries.add(insertIndex, entry);

        reassignPositions();
        int newPosition = insertIndex + 1;

        entry.markSkipped();
        pendingEvents.add(QueueEvent.taxiSkipped(id, entryId, prevStatus, prevPosition, newPosition, actor, reason, now));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void requirePublished() {
        if (lifecycleStatus != QueueLifecycleStatus.PUBLISHED) {
            throw new InvalidQueueTransitionException(
                    "Operation requires a PUBLISHED queue; current status: " + lifecycleStatus);
        }
    }

    private QueueEntry findEntry(QueueEntryId entryId) {
        return entries.stream()
                .filter(e -> e.getId().equals(entryId))
                .findFirst()
                .orElseThrow(() -> new QueueEntryNotFoundException(entryId));
    }

    private int findLastWaitingIndex() {
        int lastIndex = -1;
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getStatus() == QueueEntryStatus.WAITING) {
                lastIndex = i;
            }
        }
        return lastIndex;
    }

    private void reassignPositions() {
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setPosition(i + 1);
        }
    }

    /**
     * Returns collected events and clears the internal list.
     * Called by the application service after the aggregate operation completes.
     */
    public List<QueueEvent> drainPendingEvents() {
        List<QueueEvent> drained = new ArrayList<>(pendingEvents);
        pendingEvents.clear();
        return drained;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public DailyQueueId getId() { return id; }
    public RankRouteId getRankRouteId() { return rankRouteId; }
    public LocalDate getServiceDate() { return serviceDate; }
    public QueueLifecycleStatus getLifecycleStatus() { return lifecycleStatus; }
    public List<QueueEntry> getEntries() { return Collections.unmodifiableList(entries); }
    public long getVersion() { return version; }

    public int waitingCount() {
        return (int) entries.stream().filter(e -> e.getStatus() == QueueEntryStatus.WAITING).count();
    }
}
