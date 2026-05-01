package project.luckybooky.global.messaging.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import project.luckybooky.global.messaging.entity.OutboxEvent;
import project.luckybooky.global.messaging.entity.OutboxStatus;
import project.luckybooky.global.messaging.repository.OutboxEventRepository;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        value = "app.notification.messaging.outbox-publisher-enabled",
        havingValue = "true"
)
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxPublishWorker outboxPublishWorker;

    @Value("${app.notification.messaging.outbox-batch-size:200}")
    private int outboxBatchSize;

    @Scheduled(fixedDelayString = "${app.notification.messaging.outbox-poll-delay-ms:3000}")
    public void publishPendingMessages() {
        List<OutboxEvent> publishable = outboxEventRepository.findPublishableEvents(
                OutboxStatus.PENDING,
                LocalDateTime.now(),
                PageRequest.of(0, outboxBatchSize)
        );
        if (publishable.isEmpty()) {
            return;
        }
        publishable.forEach(event -> outboxPublishWorker.publishOneSafely(event.getId()));
    }
}
