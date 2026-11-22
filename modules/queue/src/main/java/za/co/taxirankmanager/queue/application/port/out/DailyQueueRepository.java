package za.co.taxirankmanager.queue.application.port.out;

import za.co.taxirankmanager.queue.domain.DailyQueue;
import za.co.taxirankmanager.queue.domain.DailyQueueId;
import za.co.taxirankmanager.route.domain.RankRouteId;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyQueueRepository {

    void save(DailyQueue queue);

    Optional<DailyQueue> findById(DailyQueueId id);

    Optional<DailyQueue> findByRankRouteAndDate(RankRouteId rankRouteId, LocalDate serviceDate);

    boolean existsByRankRouteAndDate(RankRouteId rankRouteId, LocalDate serviceDate);
}
