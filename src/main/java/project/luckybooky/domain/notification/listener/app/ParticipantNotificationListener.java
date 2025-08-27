package project.luckybooky.domain.notification.listener.app;

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
import project.luckybooky.domain.notification.event.app.ParticipantNotificationEvent;
import project.luckybooky.domain.notification.repository.NotificationRepository;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.repository.UserRepository;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

/**
 * 참여자 알림: 비동기 FCM 전송 및 재시도
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ParticipantNotificationListener {
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;


    private static final Set<String> sentKeys = ConcurrentHashMap.newKeySet();

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(
            retryFor = {FirebaseMessagingException.class, ExecutionException.class},
            maxAttempts = 3
    )
    public void onParticipantNotification(ParticipantNotificationEvent evt) throws Exception {
        String idKey = evt.getType() + ":" + evt.getEventId() + ":" + evt.getUserId();
        log.info("▶ 처리 시작 [{}]", idKey);

        // 1) 먼저 중복체크만
        if (sentKeys.contains(idKey)) {
            log.info("🛡️ 이미 전송됨 [{}] 스킵", idKey);
            return;
        }

        User participant = userRepository.findById(evt.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Message msg = NotificationConverter.toFcmMessageParticipant(
                participant, evt.getType(), evt.getEventName(), evt.getEventId()
        );
        if (msg == null) {
            log.warn("⚠ 토큰 미등록 [{}]", idKey);
            return;
        }

        try {
            // 2) 전송 시도
            ApiFuture<String> future = FirebaseMessaging.getInstance().sendAsync(msg);
            future.get();
            log.info("✅ 전송 성공 [{}]", idKey);

            // 3) 전송 성공 후에야 키를 추가
            sentKeys.add(idKey);

            // 4) 알림 기록 저장
            NotificationInfo info = NotificationConverter.toEntityParticipant(
                    participant, evt.getType(), evt.getEventName(), evt.getEventId()
            );
            notificationRepository.save(info);

        } catch (Exception e) {
            // 전송 실패 시 키 제거해서 retryable 동작하도록
            sentKeys.remove(idKey);
            log.error("❌ 알림 전송 오류 [{}]: {}", idKey, e.getMessage());
            throw e;
        }
    }

}