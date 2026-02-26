package project.luckybooky.domain.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.luckybooky.domain.admin.service.AdminEventUserInfoService;
import project.luckybooky.global.apiPayload.response.CommonResponse;
import project.luckybooky.global.apiPayload.response.ResultCode;

@Tag(name = "Admin Event", description = "관리자용 이벤트 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/events")
public class AdminEventController {

    private final AdminEventUserInfoService adminEventUserInfoService;

    @Operation(summary = "이벤트 참가자 유저 정보 디스코드 전송", description = "특정 이벤트의 참가자 유저 정보를 디스코드 Webhook으로 전송합니다.")
    @PostMapping("/{eventId}/participants/webhook")
    public CommonResponse<Void> sendEventUserInfoWebhook(@PathVariable Long eventId) {
        adminEventUserInfoService.sendEventUserInfoWebhook(eventId);
        return CommonResponse.of(ResultCode.OK, null);
    }
}

