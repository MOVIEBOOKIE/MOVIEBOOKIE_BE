package project.luckybooky.domain.notification.listener;

import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import project.luckybooky.domain.notification.converter.NotificationConverter;
import project.luckybooky.domain.notification.event.ParticipantNotificationEvent;
import project.luckybooky.domain.notification.service.NotificationService;
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
        User user = userRepository.findById(evt.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Message msg = NotificationConverter.toMessage(
                user,
                evt.getType().getTitle(),
                evt.getType().formatBody(evt.getEventName())
        );
        if (msg == null) {
            log.warn("FCM 토큰 미등록으로 참가자 알림 미전송: userId={}", user.getId());
            return;
        }
        notificationService.send(msg);
    }
}
