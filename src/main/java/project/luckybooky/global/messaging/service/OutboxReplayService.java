package project.luckybooky.global.messaging.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.luckybooky.global.messaging.entity.OutboxEvent;
import project.luckybooky.global.messaging.entity.OutboxStatus;
import project.luckybooky.global.messaging.repository.OutboxEventRepository;

@Service
@RequiredArgsConstructor
public class OutboxReplayService {

    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public int replayFailed(int limit) {
        int capped = Math.max(1, Math.min(limit, 1000));
        List<OutboxEvent> failedEvents = outboxEventRepository.findByStatusOrderByCreatedAtAsc(
                OutboxStatus.FAILED,
                PageRequest.of(0, capped)
        );
        failedEvents.forEach(OutboxEvent::resetForReplay);
        return failedEvents.size();
    }
}
