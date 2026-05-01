package project.luckybooky.global.messaging.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.luckybooky.global.messaging.entity.ProcessedMessage;
import project.luckybooky.global.messaging.repository.ProcessedMessageRepository;

@Service
@RequiredArgsConstructor
public class ProcessedMessageService {

    private final ProcessedMessageRepository processedMessageRepository;

    @Transactional(readOnly = true)
    public boolean isProcessed(String eventId, String consumerGroup) {
        return processedMessageRepository.existsByEventIdAndConsumerGroup(eventId, consumerGroup);
    }

    @Transactional
    public void markProcessed(
            String eventId,
            String consumerGroup,
            String topic,
            int partition,
            long offset
    ) {
        try {
            processedMessageRepository.save(
                    ProcessedMessage.builder()
                            .eventId(eventId)
                            .consumerGroup(consumerGroup)
                            .topic(topic)
                            .partitionNo(partition)
                            .offsetNo(offset)
                            .build()
            );
        } catch (DataIntegrityViolationException ignored) {
            // unique(event_id, consumer_group)로 중복 소비를 무시한다.
        }
    }
}
