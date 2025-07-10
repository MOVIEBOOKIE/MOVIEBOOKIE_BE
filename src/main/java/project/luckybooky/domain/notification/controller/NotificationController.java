package project.luckybooky.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.luckybooky.domain.notification.dto.request.FcmTokenRequestDTO;
import project.luckybooky.domain.notification.dto.request.NotificationRequestDTO;
import project.luckybooky.domain.notification.dto.response.FcmTokenResponseDTO;
import project.luckybooky.domain.notification.dto.response.NotificationPreviewDTO;
import project.luckybooky.domain.notification.dto.response.NotificationResponseDTO;
import project.luckybooky.domain.notification.service.NotificationService;
import project.luckybooky.global.apiPayload.response.CommonResponse;
import project.luckybooky.global.apiPayload.response.ResultCode;
import project.luckybooky.global.service.UserContextService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserContextService userContextService;

    @Operation(
            summary = "푸시 알림 전송",
            description = "현재 로그인한 사용자에게 저장된 FCM 토큰으로 푸시 알림을 전송합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "알림 전송 완료",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NotificationResponseDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청"),
            @ApiResponse(responseCode = "500", description = "알림 전송 중 오류 발생")
    })
    @PostMapping("/send")
    public CommonResponse<NotificationResponseDTO> sendNotification(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "전송할 알림의 제목과 내용을 담은 테스트용 알림 전송",
                    required = true,
                    content = @Content(schema = @Schema(implementation = NotificationRequestDTO.class))
            )
            @RequestBody NotificationRequestDTO requestDTO) {

        NotificationResponseDTO response = notificationService.sendNotificationToCurrentUser(requestDTO);
        return CommonResponse.of(ResultCode.OK, response);
    }

    @Operation(
            summary = "FCM 토큰 등록",
            description = "웹/앱에서 발급받은 FCM 디바이스 토큰을 서버에 등록합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 등록 완료",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FcmTokenResponseDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 토큰"),
            @ApiResponse(responseCode = "500", description = "토큰 등록 중 오류 발생")
    })
    @PostMapping("/register-token")
    public CommonResponse<FcmTokenResponseDTO> registerToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "등록할 FCM 토큰을 담은 DTO",
                    required = true,
                    content = @Content(schema = @Schema(implementation = FcmTokenRequestDTO.class))
            )
            @RequestBody FcmTokenRequestDTO dto) {

        FcmTokenResponseDTO response = notificationService.registerFcmToken(dto);
        return CommonResponse.of(ResultCode.OK, response);
    }


    @GetMapping("/notifications/host/preview/{eventId}/{code}")
    public NotificationPreviewDTO previewHostNotification(
            @PathVariable Long eventId,
            @PathVariable String code
    ) {
        return notificationService.previewHostNotification(eventId, code);
    }

    @GetMapping("/notifications/preview/participant/{eventId}/{code}")
    public NotificationPreviewDTO previewParticipantNotification(
            @PathVariable Long eventId,
            @PathVariable String code
    ) {
        return notificationService.previewParticipantNotification(eventId, code);
    }

    @Operation(summary = "알림 전체 조회",
            description = "로그인한 사용자의 모든 알림을 조회합니다.")
    @GetMapping
    public CommonResponse<List<NotificationResponseDTO>> getAll() {
        Long userId = userContextService.getUserId();
        List<NotificationResponseDTO> list = notificationService.findAllByUser(userId);
        return CommonResponse.of(ResultCode.OK, list);
    }

    @Operation(summary = "알림 삭제", description = "로그인한 사용자의 특정 알림을 삭제합니다.")
    @DeleteMapping("/{notificationsId}")
    public CommonResponse<Void> deleteOne(@PathVariable Long notificationsId) {
        Long userId = userContextService.getUserId();
        notificationService.deleteByUser(userId, notificationsId);
        return CommonResponse.of(ResultCode.OK);
    }

}
