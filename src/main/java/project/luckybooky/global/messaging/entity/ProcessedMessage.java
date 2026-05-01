package project.luckybooky.global.messaging.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "processed_message")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String eventId;

    @Column(nullable = false, length = 120)
    private String consumerGroup;

    @Column(length = 160)
    private String topic;

    private Integer partitionNo;
    private Long offsetNo;

    @Column(nullable = false)
    private LocalDateTime processedAt;

    @Builder
    private ProcessedMessage(
            String eventId,
            String consumerGroup,
            String topic,
            Integer partitionNo,
            Long offsetNo
    ) {
        this.eventId = eventId;
        this.consumerGroup = consumerGroup;
        this.topic = topic;
        this.partitionNo = partitionNo;
        this.offsetNo = offsetNo;
        this.processedAt = LocalDateTime.now();
    }
}
