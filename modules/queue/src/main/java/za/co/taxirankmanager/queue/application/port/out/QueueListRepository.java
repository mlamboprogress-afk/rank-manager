package za.co.taxirankmanager.queue.application.port.out;

import za.co.taxirankmanager.queue.domain.DailyQueue;
import za.co.taxirankmanager.route.domain.RankRouteId;

import java.util.List;

public interface QueueListRepository {

    List<DailyQueue> findByRankRoute(RankRouteId rankRouteId);
}
