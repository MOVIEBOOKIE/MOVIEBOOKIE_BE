package project.luckybooky.global.messaging.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MeterRegistry meterRegistry;
    private Counter publishSuccessCounter;
    private Counter publishFailureCounter;

    @Value("${app.notification.messaging.outbox-batch-size:200}")
    private int outboxBatchSize;

    @jakarta.annotation.PostConstruct
    void initMetrics() {
        publishSuccessCounter = Counter.builder("notification.outbox.publish.success")
                .register(meterRegistry);
        publishFailureCounter = Counter.builder("notification.outbox.publish.failure")
                .register(meterRegistry);
    }

    @Scheduled(fixedDelayString = "${app.notification.messaging.outbox-poll-delay-ms:3000}")
    public void publishPendingMessages() {
        List<OutboxEvent> publishable = outboxEventRepository.findPublishableEvents(OutboxStatus.PENDING, LocalDateTime.now());
        if (publishable.isEmpty()) {
            return;
        }
        publishable.stream()
                .limit(outboxBatchSize)
                .forEach(this::publishOneSafely);
    }

    @Transactional
    protected void publishOneSafely(OutboxEvent outboxEvent) {
        try {
            kafkaTemplate.send(
                    outboxEvent.getTopic(),
                    outboxEvent.getPartitionKey(),
                    outboxEvent.getPayload()
            ).get();
            outboxEvent.markPublished();
            publishSuccessCounter.increment();
        } catch (Exception e) {
            int nextBackoff = Math.min(60, 1 << Math.min(outboxEvent.getRetryCount(), 6));
            outboxEvent.markFailed(e.getMessage(), nextBackoff);
            publishFailureCounter.increment();
            log.warn("Outbox publish failed: eventId={}, retryCount={}, error={}",
                    outboxEvent.getEventId(),
                    outboxEvent.getRetryCount(),
                    e.getMessage());
        }
    }
}
