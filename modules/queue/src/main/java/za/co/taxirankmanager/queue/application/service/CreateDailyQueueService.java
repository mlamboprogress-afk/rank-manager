package za.co.taxirankmanager.queue.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import za.co.taxirankmanager.identity.application.port.out.CurrentUserPort;
import za.co.taxirankmanager.queue.application.command.CreateDailyQueueCommand;
import za.co.taxirankmanager.queue.application.port.in.CreateDailyQueueUseCase;
import za.co.taxirankmanager.queue.application.port.out.DailyQueueRepository;
import za.co.taxirankmanager.queue.application.port.out.QueueEventRepository;
import za.co.taxirankmanager.queue.domain.DailyQueue;
import za.co.taxirankmanager.queue.domain.DailyQueueId;
import za.co.taxirankmanager.shared.application.port.out.ClockPort;

@ApplicationScoped
public class CreateDailyQueueService implements CreateDailyQueueUseCase {

    private final DailyQueueRepository queueRepository;
    private final QueueEventRepository eventRepository;
    private final CurrentUserPort currentUser;
    private final ClockPort clock;

    @Inject
    public CreateDailyQueueService(DailyQueueRepository queueRepository,
                                   QueueEventRepository eventRepository,
                                   CurrentUserPort currentUser,
                                   ClockPort clock) {
        this.queueRepository = queueRepository;
        this.eventRepository = eventRepository;
        this.currentUser = currentUser;
        this.clock = clock;
    }

    @Override
    public DailyQueueId createQueue(CreateDailyQueueCommand command) {
        DailyQueueId id = DailyQueueId.generate();
        DailyQueue queue = DailyQueue.create(id, command.rankRouteId(), command.serviceDate(),
                currentUser.currentUserId(), clock.now());
        queueRepository.save(queue);
        eventRepository.saveAll(queue.drainPendingEvents());
        return id;
    }
}
