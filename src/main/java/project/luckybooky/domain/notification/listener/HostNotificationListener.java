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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import project.luckybooky.domain.notification.converter.NotificationConverter;
import project.luckybooky.domain.notification.event.HostNotificationEvent;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.repository.UserRepository;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;


@Component
@Slf4j
@RequiredArgsConstructor
public class HostNotificationListener {
    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;

    private static final Set<String> sentKeys = ConcurrentHashMap.newKeySet();
    private static final String DLQ_QUEUE = "notification.dlq";

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(
            value = {FirebaseMessagingException.class, ExecutionException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
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
    }

    @Recover
    public void recover(Exception e, HostNotificationEvent evt) {
        String idKey = evt.getType() + ":" + evt.getEventId() + ":" + evt.getHostUserId();
        log.error("❌ 전송 실패 [{}], DLQ 전송: {}", idKey, e.getMessage());
        rabbitTemplate.convertAndSend("", DLQ_QUEUE, evt);
    }
}