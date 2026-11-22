package za.co.taxirankmanager.queue.domain;

import za.co.taxirankmanager.fleet.domain.TaxiId;

import java.util.Objects;

/**
 * A single taxi's participation in a DailyQueue.
 * Mutation methods are package-private — only DailyQueue can change status and position.
 */
public class QueueEntry {

    private final QueueEntryId id;
    private final TaxiId taxiId;
    private int position;
    private QueueEntryStatus status;
    private long version;

    private QueueEntry(QueueEntryId id, TaxiId taxiId, int position, QueueEntryStatus status, long version) {
        this.id = Objects.requireNonNull(id);
        this.taxiId = Objects.requireNonNull(taxiId);
        this.position = position;
        this.status = Objects.requireNonNull(status);
        this.version = version;
    }

    static QueueEntry create(QueueEntryId id, TaxiId taxiId, int position) {
        return new QueueEntry(id, taxiId, position, QueueEntryStatus.WAITING, 0L);
    }

    public static QueueEntry reconstitute(QueueEntryId id, TaxiId taxiId, int position,
                                           QueueEntryStatus status, long version) {
        return new QueueEntry(id, taxiId, position, status, version);
    }

    void markDeparted() {
        this.status = QueueEntryStatus.DEPARTED;
    }

    void markSkipped() {
        this.status = QueueEntryStatus.SKIPPED;
    }

    void setPosition(int position) {
        this.position = position;
    }

    public QueueEntryId getId() { return id; }
    public TaxiId getTaxiId() { return taxiId; }
    public int getPosition() { return position; }
    public QueueEntryStatus getStatus() { return status; }
    public long getVersion() { return version; }

    public boolean isActive() {
        return status != QueueEntryStatus.DEPARTED;
    }
}
