package project.luckybooky.domain.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.luckybooky.domain.notification.dto.request.FcmTokenRequestDTO;
import project.luckybooky.domain.notification.dto.request.NotificationRequestDTO;
import project.luckybooky.domain.notification.dto.response.FcmTokenResponseDTO;
import project.luckybooky.domain.notification.dto.response.NotificationResponseDTO;
import project.luckybooky.domain.notification.service.NotificationService;
import project.luckybooky.global.apiPayload.response.CommonResponse;
import project.luckybooky.global.apiPayload.response.ResultCode;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public CommonResponse<NotificationResponseDTO> sendNotification(
            @RequestBody NotificationRequestDTO requestDTO) {

        NotificationResponseDTO response = notificationService.sendNotificationToCurrentUser(requestDTO);

        return CommonResponse.of(ResultCode.OK, response);
    }

    @PostMapping("/register-token")
    public CommonResponse<FcmTokenResponseDTO> registerToken(
            @RequestBody FcmTokenRequestDTO dto) {

        FcmTokenResponseDTO response = notificationService.registerFcmToken(dto);
        return CommonResponse.of(ResultCode.OK, response);
    }
}

