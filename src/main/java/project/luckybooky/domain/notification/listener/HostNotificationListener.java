package project.luckybooky.domain.notification.listener;

import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
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

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onHostNotification(HostNotificationEvent evt) {
        log.info("▶ 준비된 Host 알림: userId={}, type={}, eventName={}",
                evt.getHostUserId(), evt.getType(), evt.getEventName());

        User host = userRepository.findById(evt.getHostUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Message msg = NotificationConverter.toFcmMessage(host, evt.getType(), evt.getEventName(), evt.getEventId());

        if (msg == null) {
            log.warn("⚠ FCM 토큰 미등록. (보냈어야 할 메시지) title='{}', body='{}'",
                    evt.getType().getTitle(),
                    evt.getType().formatBody(evt.getEventName()));
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
