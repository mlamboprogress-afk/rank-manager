package za.co.taxirankmanager.queue.application.port.in;

import za.co.taxirankmanager.queue.domain.DailyQueue;
import za.co.taxirankmanager.queue.domain.DailyQueueId;
import za.co.taxirankmanager.route.domain.RankRouteId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GetQueueUseCase {

    Optional<DailyQueue> findById(DailyQueueId id);

    Optional<DailyQueue> findByRankRouteAndDate(RankRouteId rankRouteId, LocalDate serviceDate);

    List<DailyQueue> listByRankRoute(RankRouteId rankRouteId);
}
