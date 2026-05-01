package project.luckybooky.domain.notification.listener.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.event.entity.type.EventStatus;
import project.luckybooky.domain.event.entity.type.HostEventButtonState;
import project.luckybooky.domain.event.entity.type.ParticipantEventButtonState;
import project.luckybooky.domain.notification.converter.NotificationConverter;
import project.luckybooky.domain.notification.dto.ConfirmedData;
import project.luckybooky.domain.notification.dto.RejectedData;
import project.luckybooky.domain.notification.entity.NotificationInfo;
import project.luckybooky.domain.notification.service.MailTemplateService;
import project.luckybooky.domain.participation.entity.Participation;
import project.luckybooky.domain.participation.entity.type.ParticipateRole;
import project.luckybooky.domain.participation.repository.ParticipationRepository;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.repository.UserRepository;
import project.luckybooky.domain.user.service.AuthService;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;
import project.luckybooky.global.messaging.NotificationEventType;
import project.luckybooky.global.messaging.NotificationTopic;
import project.luckybooky.global.messaging.model.NotificationEnvelope;
import project.luckybooky.global.messaging.model.payload.DirectNotificationPayload;
import project.luckybooky.global.messaging.model.payload.HostNotificationPayload;
import project.luckybooky.global.messaging.model.payload.ParticipantNotificationPayload;
import project.luckybooky.global.messaging.model.payload.VenueConfirmedMailPayload;
import project.luckybooky.global.messaging.model.payload.VenueRejectedMailPayload;
import project.luckybooky.global.messaging.service.ProcessedMessageService;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.notification.messaging.mode", havingValue = "kafka")
public class NotificationKafkaConsumer {

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final ParticipationRepository participationRepository;
    private final project.luckybooky.domain.notification.repository.NotificationRepository notificationRepository;
    private final MailTemplateService mailTemplateService;
    private final ProcessedMessageService processedMessageService;

    @Value("${spring.kafka.consumer.group-id:moviebookie-notification}")
    private String consumerGroupId;

    @Value("${app.home-url}")
    private String homeUrl;

    @KafkaListener(
            topics = NotificationTopic.PUSH,
            groupId = "${spring.kafka.consumer.group-id:moviebookie-notification}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consumePush(
            String rawMessage,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        NotificationEnvelope envelope = parseEnvelope(rawMessage);
        if (processedMessageService.isProcessed(envelope.eventId(), consumerGroupId)) {
            return;
        }

        JsonNode payloadNode = objectMapper.valueToTree(envelope.payload());
        switch (envelope.eventType()) {
            case NotificationEventType.HOST_PUSH -> processHostPush(payloadNode);
            case NotificationEventType.PARTICIPANT_PUSH -> processParticipantPush(payloadNode);
            case NotificationEventType.DIRECT_PUSH -> processDirectPush(payloadNode);
            default -> throw new IllegalStateException("Unsupported push event type: " + envelope.eventType());
        }

        processedMessageService.markProcessed(envelope.eventId(), consumerGroupId, topic, partition, offset);
    }

    @KafkaListener(
            topics = NotificationTopic.MAIL,
            groupId = "${spring.kafka.consumer.group-id:moviebookie-notification}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consumeMail(
            String rawMessage,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        NotificationEnvelope envelope = parseEnvelope(rawMessage);
        if (processedMessageService.isProcessed(envelope.eventId(), consumerGroupId)) {
            return;
        }

        JsonNode payloadNode = objectMapper.valueToTree(envelope.payload());
        switch (envelope.eventType()) {
            case NotificationEventType.VENUE_CONFIRMED_MAIL -> processVenueConfirmedMail(payloadNode);
            case NotificationEventType.VENUE_REJECTED_MAIL -> processVenueRejectedMail(payloadNode);
            default -> throw new IllegalStateException("Unsupported mail event type: " + envelope.eventType());
        }

        processedMessageService.markProcessed(envelope.eventId(), consumerGroupId, topic, partition, offset);
    }

    private void processHostPush(JsonNode payloadNode) {
        HostNotificationPayload payload = objectMapper.convertValue(payloadNode, HostNotificationPayload.class);
        User host = userRepository.findById(payload.hostUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Message message = NotificationConverter.toFcmMessage(host, payload.type(), payload.eventName(), payload.eventId());
        if (message == null) {
            return;
        }
        sendAndSave(
                message,
                NotificationConverter.toEntity(host, payload.type(), payload.eventName(), payload.eventId())
        );
    }

    private void processParticipantPush(JsonNode payloadNode) {
        ParticipantNotificationPayload payload = objectMapper.convertValue(payloadNode, ParticipantNotificationPayload.class);
        User participant = userRepository.findById(payload.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Message message = NotificationConverter.toFcmMessageParticipant(
                participant,
                payload.type(),
                payload.eventName(),
                payload.eventId()
        );
        if (message == null) {
            return;
        }
        sendAndSave(
                message,
                NotificationConverter.toEntityParticipant(participant, payload.type(), payload.eventName(), payload.eventId())
        );
    }

    private void processDirectPush(JsonNode payloadNode) {
        DirectNotificationPayload payload = objectMapper.convertValue(payloadNode, DirectNotificationPayload.class);
        User user = userRepository.findById(payload.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Message message = NotificationConverter.toMessage(user, payload.title(), payload.body());
        if (message == null) {
            return;
        }

        NotificationInfo info = NotificationInfo.builder()
                .user(user)
                .title(payload.title())
                .body(payload.body())
                .eventId(payload.eventId())
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .build();
        sendAndSave(message, info);
    }

    private void processVenueConfirmedMail(JsonNode payloadNode) {
        if (AuthService.isUserWithdrawalInProgress()) {
            return;
        }
        VenueConfirmedMailPayload payload = objectMapper.convertValue(payloadNode, VenueConfirmedMailPayload.class);
        Participation hostPart = participationRepository
                .findByUser_IdAndEvent_IdAndParticipateRole(
                        payload.hostUserId(),
                        payload.eventId(),
                        ParticipateRole.HOST
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPATION_NOT_FOUND));

        ConfirmedData data = NotificationConverter.toConfirmedData(hostPart, homeUrl);
        mailTemplateService.sendVenueConfirmedMail(hostPart.getUser().getEmail(), data);
    }

    private void processVenueRejectedMail(JsonNode payloadNode) {
        if (AuthService.isUserWithdrawalInProgress()) {
            return;
        }
        VenueRejectedMailPayload payload = objectMapper.convertValue(payloadNode, VenueRejectedMailPayload.class);
        Participation hostPart = participationRepository
                .findByUser_IdAndEvent_IdAndParticipateRole(
                        payload.hostUserId(),
                        payload.eventId(),
                        ParticipateRole.HOST
                )
                .orElse(null);
        if (hostPart == null) {
            return;
        }

        Event event = hostPart.getEvent();
        boolean shouldSend =
                event.getEventStatus() == EventStatus.VENUE_RESERVATION_CANCELED
                        && event.getHostEventButtonState() == HostEventButtonState.VENUE_RESERVATION_CANCELED
                        && event.getParticipantEventButtonState() == ParticipantEventButtonState.VENUE_RESERVATION_CANCELED;
        if (!shouldSend) {
            return;
        }

        RejectedData data = NotificationConverter.toRejectedData(hostPart, homeUrl);
        String to = hostPart.getUser().getCertificationEmail();
        mailTemplateService.sendVenueRejectedMail(to, data);
    }

    private NotificationEnvelope parseEnvelope(String rawMessage) {
        try {
            return objectMapper.readValue(rawMessage, NotificationEnvelope.class);
        } catch (Exception e) {
            log.error("Failed to parse Kafka envelope", e);
            throw new IllegalStateException("Invalid Kafka message payload", e);
        }
    }

    private void sendAndSave(Message message, NotificationInfo info) {
        try {
            FirebaseMessaging.getInstance().send(message);
            notificationRepository.save(info);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to send notification", e);
        }
    }
}
