package za.co.taxirankmanager.queue.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import za.co.taxirankmanager.queue.application.port.in.GetQueueUseCase;
import za.co.taxirankmanager.queue.application.port.out.DailyQueueRepository;
import za.co.taxirankmanager.queue.application.port.out.QueueListRepository;
import za.co.taxirankmanager.queue.domain.DailyQueue;
import za.co.taxirankmanager.queue.domain.DailyQueueId;
import za.co.taxirankmanager.route.domain.RankRouteId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class GetQueueService implements GetQueueUseCase {

    private final DailyQueueRepository queueRepository;
    private final QueueListRepository queueListRepository;

    @Inject
    public GetQueueService(DailyQueueRepository queueRepository, QueueListRepository queueListRepository) {
        this.queueRepository = queueRepository;
        this.queueListRepository = queueListRepository;
    }

    @Override
    public Optional<DailyQueue> findById(DailyQueueId id) {
        return queueRepository.findById(id);
    }

    @Override
    public Optional<DailyQueue> findByRankRouteAndDate(RankRouteId rankRouteId, LocalDate serviceDate) {
        return queueRepository.findByRankRouteAndDate(rankRouteId, serviceDate);
    }

    @Override
    public List<DailyQueue> listByRankRoute(RankRouteId rankRouteId) {
        return queueListRepository.findByRankRoute(rankRouteId);
    }
}
