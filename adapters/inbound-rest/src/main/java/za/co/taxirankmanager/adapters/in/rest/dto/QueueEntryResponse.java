package za.co.taxirankmanager.adapters.in.rest.dto;

import za.co.taxirankmanager.queue.domain.QueueEntry;

public record QueueEntryResponse(String id, String taxiId, int position, String status) {

    public static QueueEntryResponse from(QueueEntry entry) {
        return new QueueEntryResponse(
                entry.getId().value().toString(),
                entry.getTaxiId().value().toString(),
                entry.getPosition(),
                entry.getStatus().name());
    }
}
