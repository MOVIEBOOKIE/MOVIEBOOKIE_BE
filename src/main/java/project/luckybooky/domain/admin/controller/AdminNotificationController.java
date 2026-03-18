package project.luckybooky.domain.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.luckybooky.domain.admin.dto.AdminGlobalNotificationRequest;
import project.luckybooky.domain.admin.dto.AdminGlobalNotificationResponse;
import project.luckybooky.domain.admin.service.AdminGlobalNotificationService;
import project.luckybooky.domain.adminUser.service.AdminContextService;
import project.luckybooky.global.apiPayload.response.CommonResponse;
import project.luckybooky.global.apiPayload.response.ResultCode;

@Tag(name = "Admin Notification", description = "관리자 공지/푸시 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/notifications")
public class AdminNotificationController {

    private final AdminGlobalNotificationService adminGlobalNotificationService;
    private final AdminContextService adminContextService;

    @Operation(summary = "전체 유저 공지/푸시 배치 발송", description = "DB의 전체 유저를 batchSize 단위로 나누어 공지/푸시를 일괄 발송합니다.")
    @PostMapping("/bulk/users")
    public CommonResponse<AdminGlobalNotificationResponse> sendToAllUsers(
            @Valid @RequestBody AdminGlobalNotificationRequest request
    ) {
        adminContextService.getCurrentAdminUser();
        AdminGlobalNotificationResponse response = adminGlobalNotificationService.sendToAllUsers(request);
        return CommonResponse.of(ResultCode.OK, response);
    }
}
