package project.luckybooky.domain.notification.push.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import project.luckybooky.domain.notification.push.converter.NotificationConverter;
import project.luckybooky.domain.notification.push.dto.request.FcmTokenRequestDTO;
import project.luckybooky.domain.notification.push.dto.request.NotificationRequestDTO;
import project.luckybooky.domain.notification.push.dto.response.FcmTokenResponseDTO;
import project.luckybooky.domain.notification.push.dto.response.NotificationResponseDTO;
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

    public NotificationResponseDTO sendNotificationToCurrentUser(NotificationRequestDTO requestDTO) {
        String email = AuthenticatedUserUtils.getAuthenticatedUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getFcmToken() == null) {
            throw new BusinessException(ErrorCode.NOTIFICATION_FCM_TOKEN_NOT_FOUND);
        }

        Message message = NotificationConverter.toFcmMessage(user, requestDTO);

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("✅ FCM 알림 전송 성공: {}", response);
            return new NotificationResponseDTO("success", "알림 전송 완료");
        } catch (Exception e) {
            log.error("❌ FCM 알림 전송 실패", e);
            throw new BusinessException(ErrorCode.NOTIFICATION_SEND_FAILED);
        }
    }

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

}
