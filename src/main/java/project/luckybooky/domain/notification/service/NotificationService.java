package project.luckybooky.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.luckybooky.domain.event.repository.EventRepository;
import project.luckybooky.domain.notification.converter.NotificationConverter;
import project.luckybooky.domain.notification.dto.request.FcmTokenRequestDTO;
import project.luckybooky.domain.notification.dto.request.NotificationRequestDTO;
import project.luckybooky.domain.notification.dto.response.FcmTokenResponseDTO;
import project.luckybooky.domain.notification.dto.response.NotificationResponseDTO;
import project.luckybooky.domain.notification.dto.response.SendNotificationResponseDTO;
import project.luckybooky.domain.notification.entity.NotificationInfo;
import project.luckybooky.domain.notification.repository.NotificationRepository;
import project.luckybooky.domain.participation.repository.ParticipationRepository;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.repository.UserRepository;
import project.luckybooky.domain.user.util.AuthenticatedUserUtils;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    @Getter
    private final ParticipationRepository participationRepository;
    private final NotificationRepository notificationRepository;

    public FcmTokenResponseDTO registerFcmToken(FcmTokenRequestDTO dto) {
        String email = AuthenticatedUserUtils.getAuthenticatedUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        try {
            user.setFcmToken(dto.getToken());
            userRepository.save(user);
            return new FcmTokenResponseDTO("success", "FCM 토큰 등록 완료");
        } catch (Exception e) {
            log.error("FCM 토큰 등록 실패", e);
            throw new BusinessException(ErrorCode.FCM_TOKEN_REGISTER_FAILED);
        }
    }

    public void send(Message message) {
        try {
            String resp = FirebaseMessaging.getInstance().send(message);
            log.info("FCM 전송 성공: {}", resp);
        } catch (Exception e) {
            log.error("FCM 전송 중 오류가 발생했지만, 서비스에는 영향을 주지 않습니다.", e);
        }
    }

    public SendNotificationResponseDTO sendNotificationToCurrentUser(NotificationRequestDTO requestDTO) {
        String email = AuthenticatedUserUtils.getAuthenticatedUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2) 메시지 생성 (토큰 없으면 null)
        Message message = NotificationConverter.toMessage(
                user,
                requestDTO.getTitle(),
                requestDTO.getBody()
        );
        if (message == null) {
            log.warn("FCM 토큰이 없어 알림 전송을 건너뜁니다. userEmail={}", email);
            return new SendNotificationResponseDTO("skipped", "FCM 토큰이 없어 전송하지 않았습니다.");
        }

        // 3) FCM 전송 시도
        try {
            String resp = FirebaseMessaging.getInstance().send(message);
            log.info("FCM 전송 성공: 사용자={}, 응답={}", email, resp);
        } catch (Exception e) {
            log.error("FCM 전송 중 오류 발생: 사용자={}, msg={}", email, e.getMessage(), e);
        }

        // 4) NotificationInfo 생성 및 저장
        NotificationInfo info = NotificationInfo.builder()
                .user(user)
                .title(requestDTO.getTitle())
                .body(requestDTO.getBody())
                .eventId(requestDTO.getEventId())
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .build();
        log.debug("저장할 NotificationInfo 준비: userId={}, title='{}', body='{}', eventId={}",
                user.getId(), info.getTitle(), info.getBody(), info.getEventId());

        try {
            NotificationInfo saved = notificationRepository.save(info);
            log.info("알림 내역 저장 성공: id={}, userId={}, sentAt={}",
                    saved.getId(), saved.getUser().getId(), saved.getSentAt());
        } catch (Exception e) {
            log.error("알림 내역 저장 중 오류 발생: userId={}, err={}", user.getId(), e.getMessage(), e);
            throw new BusinessException(ErrorCode.NOTIFICATION_SAVE_FAILED);
        }

        return new SendNotificationResponseDTO("success", "알림 전송 및 저장 완료");
    }

    /**
     * 전체 알림 조회 → 레포지토리 사용
     */
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> findAllByUser(Long userId) {
        return notificationRepository.findByUserIdOrderBySentAtDesc(userId)
                .stream()
                .map(NotificationResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 알림 삭제
     */
    @Transactional
    public void deleteByUser(Long userId, Long notificationId) {
        int deleted = notificationRepository.deleteByUserIdAndId(userId, notificationId);
        if (deleted == 0) {
            // 해당 사용자의 알림이 아니거나, 존재하지 않는 알림인 경우
            throw new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }
    }

    /**
     * 알림 읽음으로 바꾸는 메서드
     */
    @Transactional
    public void markRead(Long userId, Long notificationId) {
        int updated = notificationRepository.updateReadStatus(userId, notificationId);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }
    }

}
