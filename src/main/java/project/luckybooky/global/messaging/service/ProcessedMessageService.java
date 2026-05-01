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

    @Transactional
    public boolean tryAcquireForProcessing(
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
            return true;
        } catch (DataIntegrityViolationException ignored) {
            // unique(event_id, consumer_group) 선점 실패 -> 이미 처리 중/완료된 메시지
            return false;
        }
    }
}
