package za.co.taxirankmanager.adapters.in.rest.dto;

import za.co.taxirankmanager.queue.domain.DailyQueue;

import java.util.List;

public record QueueResponse(
        String id,
        String rankRouteId,
        String serviceDate,
        String status,
        long version,
        int waitingCount,
        List<QueueEntryResponse> entries
) {
    public static QueueResponse from(DailyQueue queue) {
        List<QueueEntryResponse> entries = queue.getEntries().stream()
                .map(QueueEntryResponse::from)
                .toList();
        return new QueueResponse(
                queue.getId().value().toString(),
                queue.getRankRouteId().value().toString(),
                queue.getServiceDate().toString(),
                queue.getLifecycleStatus().name(),
                queue.getVersion(),
                queue.waitingCount(),
                entries);
    }
}
