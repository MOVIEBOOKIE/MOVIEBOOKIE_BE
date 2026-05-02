package project.luckybooky.global.messaging.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import project.luckybooky.global.messaging.entity.OutboxEvent;
import project.luckybooky.global.messaging.entity.OutboxStatus;
import project.luckybooky.global.messaging.repository.OutboxEventRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublishWorker {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MeterRegistry meterRegistry;

    private Counter publishSuccessCounter;
    private Counter publishFailureCounter;

    @PostConstruct
    void initMetrics() {
        publishSuccessCounter = Counter.builder("notification.outbox.publish.success")
                .register(meterRegistry);
        publishFailureCounter = Counter.builder("notification.outbox.publish.failure")
                .register(meterRegistry);
    }

    @Transactional
    public void publishOneSafely(Long outboxEventId) {
        LocalDateTime now = LocalDateTime.now();
        int claimed = outboxEventRepository.claimForPublishing(
                outboxEventId,
                OutboxStatus.PENDING,
                OutboxStatus.PUBLISHING,
                now
        );
        if (claimed == 0) {
            return;
        }

        OutboxEvent outboxEvent = outboxEventRepository.findById(outboxEventId).orElse(null);
        if (outboxEvent == null) {
            return;
        }

        if (outboxEvent.getStatus() != OutboxStatus.PUBLISHING) {
            return;
        }

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
