package project.luckybooky.domain.notification.listener;

import com.google.api.core.ApiFuture;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import project.luckybooky.domain.notification.converter.NotificationConverter;
import project.luckybooky.domain.notification.entity.NotificationInfo;
import project.luckybooky.domain.notification.event.HostNotificationEvent;
import project.luckybooky.domain.notification.repository.NotificationRepository;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.repository.UserRepository;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

/**
 * 호스트 알림: 비동기 FCM 전송 및 재시도
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class HostNotificationListener {
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;


    private static final Set<String> sentKeys = ConcurrentHashMap.newKeySet();

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(
            value = {FirebaseMessagingException.class, ExecutionException.class},
            maxAttempts = 3
    )
    public void onHostNotification(HostNotificationEvent evt) throws Exception {
        String idKey = evt.getType() + ":" + evt.getEventId() + ":" + evt.getHostUserId();
        log.info("▶ 처리 시작 [{}]", idKey);

        if (!sentKeys.add(idKey)) {
            log.info("🛡️ 이미 전송됨 [{}] 스킵", idKey);
            return;
        }

        User host = userRepository.findById(evt.getHostUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Message msg = NotificationConverter.toFcmMessage(host, evt.getType(), evt.getEventName(), evt.getEventId());
        if (msg == null) {
            log.warn("⚠ 토큰 미등록 [{}]", idKey);
            return;
        }

        ApiFuture<String> future = FirebaseMessaging.getInstance().sendAsync(msg);
        future.get();

        log.info("✅ 전송 성공 [{}]", idKey);

        NotificationInfo info = NotificationConverter.toEntity(
                host,
                evt.getType(),
                evt.getEventName(),
                evt.getEventId()
        );
        notificationRepository.save(info);
        log.info("💾 알림 내역 저장 완료: notificationId={}, hostId={}", info.getId(), host.getId());
    }
}
