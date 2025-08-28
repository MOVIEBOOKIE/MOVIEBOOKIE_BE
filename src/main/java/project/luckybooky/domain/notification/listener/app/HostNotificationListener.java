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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import project.luckybooky.domain.notification.converter.NotificationConverter;
import project.luckybooky.domain.notification.dto.ConfirmedData;
import project.luckybooky.domain.notification.entity.NotificationInfo;
import project.luckybooky.domain.notification.event.app.HostNotificationEvent;
import project.luckybooky.domain.notification.repository.NotificationRepository;
import project.luckybooky.domain.notification.service.MailTemplateService;
import project.luckybooky.domain.participation.entity.Participation;
import project.luckybooky.domain.participation.entity.type.ParticipateRole;
import project.luckybooky.domain.participation.repository.ParticipationRepository;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.repository.UserRepository;
import project.luckybooky.domain.user.service.AuthService;
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
    private final ParticipationRepository participationRepository;
    private final MailTemplateService mailTemplateService;

    @Value("${app.home-url}")
    private String homeUrl;

    private static final Set<String> sentKeys = ConcurrentHashMap.newKeySet();

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(
            retryFor = {FirebaseMessagingException.class, ExecutionException.class},
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
        try {
            notificationRepository.save(info);
        } catch (BusinessException e) {
            log.error("❌ 알림 내역 저장 실패: hostId={}, error={}", host.getId(), e.getMessage(), e);
        }
    }

    @EventListener
    @Transactional(readOnly = true)
    public void handleHostNotification(HostNotificationEvent evt) {
        String key = evt.getType() + ":" + evt.getEventId() + ":" + evt.getHostUserId();
        log.info("▶ HostNotification start [{}]", key);

        // 회원탈퇴 시에는 메일을 발송하지 않음
        if (AuthService.isUserWithdrawalInProgress()) {
            log.info("🛡️ 회원탈퇴로 인한 이벤트 취소 - 호스트 메일 발송 생략: eventId={}, hostUserId={}", 
                    evt.getEventId(), evt.getHostUserId());
            return;
        }

        Participation hostPart = participationRepository
                .findByUser_IdAndEvent_IdAndParticipateRole(
                        evt.getHostUserId(),
                        evt.getEventId(),
                        ParticipateRole.HOST
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPATION_NOT_FOUND));

        ConfirmedData data = NotificationConverter.toConfirmedData(hostPart, homeUrl);

        mailTemplateService.sendVenueConfirmedMail(
                hostPart.getUser().getEmail(),
                data
        );
        log.info("✅ Mail sent [{}]", key);
    }
}
