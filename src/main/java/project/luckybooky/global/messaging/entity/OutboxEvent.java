package project.luckybooky.global.messaging.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "outbox_event")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String eventId;

    @Column(nullable = false, length = 191)
    private String aggregateKey;

    @Column(nullable = false, length = 120)
    private String eventType;

    @Column(nullable = false, length = 160)
    private String topic;

    @Column(length = 191)
    private String partitionKey;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OutboxStatus status;

    @Column(nullable = false)
    private int retryCount;

    @Column(nullable = false)
    private int maxRetryCount;

    private LocalDateTime nextRetryAt;
    private String lastError;
    private LocalDateTime publishedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private OutboxEvent(
            String eventId,
            String aggregateKey,
            String eventType,
            String topic,
            String partitionKey,
            String payload,
            int maxRetryCount
    ) {
        LocalDateTime now = LocalDateTime.now();
        this.eventId = eventId;
        this.aggregateKey = aggregateKey;
        this.eventType = eventType;
        this.topic = topic;
        this.partitionKey = partitionKey;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.retryCount = 0;
        this.maxRetryCount = maxRetryCount;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void markPublished() {
        LocalDateTime now = LocalDateTime.now();
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = now;
        this.updatedAt = now;
        this.nextRetryAt = null;
        this.lastError = null;
    }

    public void markFailed(String errorMessage, int backoffSeconds) {
        LocalDateTime now = LocalDateTime.now();
        this.retryCount += 1;
        this.lastError = trimError(errorMessage);
        this.updatedAt = now;
        if (this.retryCount >= this.maxRetryCount) {
            this.status = OutboxStatus.FAILED;
            this.nextRetryAt = null;
            return;
        }

        this.status = OutboxStatus.PENDING;
        this.nextRetryAt = now.plusSeconds(Math.max(backoffSeconds, 1));
    }

    public void resetForReplay() {
        this.status = OutboxStatus.PENDING;
        this.nextRetryAt = null;
        this.lastError = null;
        this.updatedAt = LocalDateTime.now();
    }

    private String trimError(String message) {
        if (message == null) {
            return null;
        }
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }
}
