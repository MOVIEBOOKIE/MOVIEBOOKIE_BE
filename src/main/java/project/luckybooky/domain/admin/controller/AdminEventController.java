package project.luckybooky.domain.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.luckybooky.domain.admin.dto.AdminBulkNotificationRequest;
import project.luckybooky.domain.admin.dto.AdminBulkNotificationResponse;
import project.luckybooky.domain.admin.dto.AdminEventParticipantsResponse;
import project.luckybooky.domain.admin.dto.AdminEventUpdateRequest;
import project.luckybooky.domain.admin.dto.AdminEventUpdateResponse;
import project.luckybooky.domain.admin.service.AdminBulkNotificationService;
import project.luckybooky.domain.admin.service.AdminEventUserInfoService;
import project.luckybooky.domain.admin.service.AdminEventManagementService;
import project.luckybooky.domain.adminUser.service.AdminContextService;
import project.luckybooky.domain.event.dto.response.EventResponse;
import project.luckybooky.global.apiPayload.response.CommonResponse;
import project.luckybooky.global.apiPayload.response.ResultCode;

@Tag(name = "Admin Event", description = "관리자용 이벤트 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/events")
public class AdminEventController {

    private final AdminEventUserInfoService adminEventUserInfoService;
    private final AdminEventManagementService adminEventManagementService;
    private final AdminBulkNotificationService adminBulkNotificationService;
    private final AdminContextService adminContextService;

    @Operation(summary = "이벤트 참가자 유저 정보 디스코드 전송", description = "특정 이벤트의 참가자 유저 정보를 디스코드 Webhook으로 전송합니다.")
    @PostMapping("/{eventId}/participants/webhook")
    public CommonResponse<Void> sendEventUserInfoWebhook(@PathVariable Long eventId) {
        adminContextService.getCurrentAdminUser();
        adminEventUserInfoService.sendEventUserInfoWebhook(eventId);
        return CommonResponse.of(ResultCode.OK, null);
    }

    @Operation(summary = "관리자 이벤트 대관 확정", description = "관리자가 특정 이벤트의 대관을 확정합니다.")
    @PostMapping("/{eventId}/venue-confirmed")
    public CommonResponse<EventResponse.EventVenueConfirmedResultDTO> confirmVenue(@PathVariable Long eventId) {
        adminContextService.getCurrentAdminUser();
        EventResponse.EventVenueConfirmedResultDTO response = adminEventManagementService.confirmVenue(eventId);
        return CommonResponse.of(ResultCode.OK, response);
    }

    @Operation(summary = "관리자 이벤트 참여자 정보 조회", description = "특정 이벤트의 참여자 목록을 JSON으로 조회합니다.")
    @GetMapping("/{eventId}/participants")
    public CommonResponse<AdminEventParticipantsResponse> getParticipants(@PathVariable Long eventId) {
        adminContextService.getCurrentAdminUser();
        AdminEventParticipantsResponse response = adminEventManagementService.getParticipants(eventId);
        return CommonResponse.of(ResultCode.OK, response);
    }

    @Operation(summary = "관리자 이벤트 정보 수정", description = "관리자가 이벤트 정보를 부분 수정합니다.")
    @PatchMapping("/{eventId}")
    public CommonResponse<AdminEventUpdateResponse> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody AdminEventUpdateRequest request
    ) {
        adminContextService.getCurrentAdminUser();
        AdminEventUpdateResponse response = adminEventManagementService.updateEvent(eventId, request);
        return CommonResponse.of(ResultCode.OK, response);
    }

    @Operation(summary = "관리자 이벤트 공지/푸시 일괄 발송", description = "특정 이벤트의 호스트/참여자/전체 대상으로 공지 및 푸시를 일괄 발송합니다.")
    @PostMapping("/{eventId}/notifications/bulk")
    public CommonResponse<AdminBulkNotificationResponse> sendBulkNotification(
            @PathVariable Long eventId,
            @Valid @RequestBody AdminBulkNotificationRequest request
    ) {
        adminContextService.getCurrentAdminUser();
        AdminBulkNotificationResponse response = adminBulkNotificationService.sendBulkNotification(eventId, request);
        return CommonResponse.of(ResultCode.OK, response);
    }
}

