package project.luckybooky.global.messaging.model;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record NotificationEnvelope(
        String eventId,
        String eventType,
        LocalDateTime occurredAt,
        String aggregateKey,
        String traceId,
        Object payload
) {
}
