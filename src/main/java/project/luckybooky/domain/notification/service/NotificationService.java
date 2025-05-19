package project.luckybooky.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import project.luckybooky.domain.notification.converter.NotificationConverter;
import project.luckybooky.domain.notification.dto.request.FcmTokenRequestDTO;
import project.luckybooky.domain.notification.dto.request.NotificationRequestDTO;
import project.luckybooky.domain.notification.dto.response.FcmTokenResponseDTO;
import project.luckybooky.domain.notification.dto.response.NotificationResponseDTO;
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
            log.error("FCM 전송 실패", e);
            throw new BusinessException(ErrorCode.NOTIFICATION_SEND_FAILED);
        }
    }

    public NotificationResponseDTO sendNotificationToCurrentUser(NotificationRequestDTO requestDTO) {
        // 1) 현재 로그인한 사용자 조회
        String email = AuthenticatedUserUtils.getAuthenticatedUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2) FCM 메시지 객체 생성 (Converter에 위임)
        Message message = NotificationConverter.toMessage(
                user,
                requestDTO.getTitle(),
                requestDTO.getBody()
        );

        // 3) 실제 전송
        try {
            String resp = FirebaseMessaging.getInstance().send(message);
            log.info("FCM 전송 성공: {}", resp);
        } catch (Exception e) {
            log.error("FCM 전송 실패", e);
            throw new BusinessException(ErrorCode.NOTIFICATION_SEND_FAILED);
        }

        // 4) 컨트롤러에 반환할 DTO
        return new NotificationResponseDTO("success", "알림 전송 완료");
    }

}
