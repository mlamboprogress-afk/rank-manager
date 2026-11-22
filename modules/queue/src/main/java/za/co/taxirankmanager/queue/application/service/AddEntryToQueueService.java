package za.co.taxirankmanager.queue.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import za.co.taxirankmanager.identity.application.port.out.CurrentUserPort;
import za.co.taxirankmanager.queue.application.command.AddEntryToQueueCommand;
import za.co.taxirankmanager.queue.application.port.in.AddEntryToQueueUseCase;
import za.co.taxirankmanager.queue.application.port.out.DailyQueueRepository;
import za.co.taxirankmanager.queue.application.port.out.QueueEventRepository;
import za.co.taxirankmanager.queue.domain.DailyQueue;
import za.co.taxirankmanager.queue.domain.QueueEntryId;
import za.co.taxirankmanager.shared.application.port.out.ClockPort;

@ApplicationScoped
public class AddEntryToQueueService implements AddEntryToQueueUseCase {

    private final DailyQueueRepository queueRepository;
    private final QueueEventRepository eventRepository;
    private final CurrentUserPort currentUser;
    private final ClockPort clock;

    @Inject
    public AddEntryToQueueService(DailyQueueRepository queueRepository,
                                  QueueEventRepository eventRepository,
                                  CurrentUserPort currentUser,
                                  ClockPort clock) {
        this.queueRepository = queueRepository;
        this.eventRepository = eventRepository;
        this.currentUser = currentUser;
        this.clock = clock;
    }

    @Override
    public QueueEntryId addEntry(AddEntryToQueueCommand command) {
        DailyQueue queue = queueRepository.findById(command.queueId())
                .orElseThrow(() -> new IllegalArgumentException("Queue not found: " + command.queueId()));
        QueueEntryId entryId = QueueEntryId.generate();
        queue.addEntry(entryId, command.taxiId(), currentUser.currentUserId(), clock.now());
        queueRepository.save(queue);
        eventRepository.saveAll(queue.drainPendingEvents());
        return entryId;
    }
}
