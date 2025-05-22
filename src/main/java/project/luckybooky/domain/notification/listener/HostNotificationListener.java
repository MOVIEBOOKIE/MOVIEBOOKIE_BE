package project.luckybooky.domain.notification.listener;

import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import project.luckybooky.domain.notification.converter.NotificationConverter;
import project.luckybooky.domain.notification.event.HostNotificationEvent;
import project.luckybooky.domain.notification.service.NotificationService;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.repository.UserRepository;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@Component
@Slf4j
@RequiredArgsConstructor
public class HostNotificationListener {
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @EventListener
    public void onHostNotification(HostNotificationEvent evt) {
        User host = userRepository.findById(evt.getHostUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 메시지 생성 (토큰 없으면 null)
        Message msg = NotificationConverter.toFcmMessage(
                host, evt.getType(), evt.getEventName());

        if (msg == null) {
            log.warn("FCM 토큰 미등록으로 알림을 전송하지 않습니다. userId={}", host.getId());
            return;
        }

        // 전송 시도 (실패해도 예외는 흘려보내지 않음)
        notificationService.send(msg);
    }
}

