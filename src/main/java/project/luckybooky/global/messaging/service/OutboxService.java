package project.luckybooky.global.messaging.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.luckybooky.global.messaging.entity.OutboxEvent;
import project.luckybooky.global.messaging.model.NotificationEnvelope;
import project.luckybooky.global.messaging.repository.OutboxEventRepository;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private static final int DEFAULT_MAX_RETRY = 10;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public String enqueue(
            String topic,
            String eventType,
            String aggregateKey,
            String partitionKey,
            Object payload
    ) {
        String eventId = UUID.randomUUID().toString();
        NotificationEnvelope envelope = NotificationEnvelope.builder()
                .eventId(eventId)
                .eventType(eventType)
                .occurredAt(LocalDateTime.now())
                .aggregateKey(aggregateKey)
                .traceId(MDC.get("traceId"))
                .payload(payload)
                .build();

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .eventId(eventId)
                .aggregateKey(aggregateKey)
                .eventType(eventType)
                .topic(topic)
                .partitionKey(partitionKey)
                .payload(toJson(envelope))
                .maxRetryCount(DEFAULT_MAX_RETRY)
                .build();
        outboxEventRepository.save(outboxEvent);
        return eventId;
    }

    private String toJson(NotificationEnvelope envelope) {
        try {
            return objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize outbox envelope", e);
        }
    }
}
