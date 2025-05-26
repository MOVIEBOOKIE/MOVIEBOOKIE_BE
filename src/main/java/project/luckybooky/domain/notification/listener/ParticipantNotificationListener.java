package project.luckybooky.domain.notification.listener;

import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import project.luckybooky.domain.notification.converter.NotificationConverter;
import project.luckybooky.domain.notification.event.ParticipantNotificationEvent;
import project.luckybooky.domain.notification.service.NotificationService;
import project.luckybooky.domain.notification.type.ParticipantNotificationType;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.repository.UserRepository;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@Component
@Slf4j
@RequiredArgsConstructor
public class ParticipantNotificationListener {
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @EventListener
    public void onParticipantNotification(ParticipantNotificationEvent evt) {

        Long participantId = evt.getUserId();
        ParticipantNotificationType type = evt.getType();
        String eventName = evt.getEventName();

        User participant = userRepository.findById(participantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Message msg = NotificationConverter.toFcmMessageParticipant(participant, type, eventName);

        log.info("📨 ParticipantNotification 시도: userId={} title='{}' body='{}'",
                participantId,
                type.getTitle(),
                type.formatBody(eventName));

        log.info("📨 ParticipantNotification 시도: userId={} title='{}' body='{}'",
                participant.getId(),
                evt.getType().getTitle(),
                evt.getType().formatBody(evt.getEventName()));

        if (msg == null) {
            log.warn("⚠️ FCM 토큰 미등록으로 알림을 전송하지 않습니다. userId={}", participant.getId());
            return;
        }

        try {
            notificationService.send(msg);
            log.info("✅ FCM 전송 성공: title='{}'  body='{}'",
                    evt.getType().getTitle(),
                    evt.getType().formatBody(evt.getEventName()));
        } catch (Exception e) {
            log.error("❌ FCM 전송 실패: title='{}'  body='{}'",
                    evt.getType().getTitle(),
                    evt.getType().formatBody(evt.getEventName()),
                    e);
        }
    }
}
