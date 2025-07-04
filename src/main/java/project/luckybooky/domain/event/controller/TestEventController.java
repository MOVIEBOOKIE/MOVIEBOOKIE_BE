package project.luckybooky.domain.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.luckybooky.domain.event.dto.response.EventParticipantsResponse;
import project.luckybooky.domain.event.service.EventService;
import project.luckybooky.global.apiPayload.response.CommonResponse;
import project.luckybooky.global.apiPayload.response.ResultCode;

@RestController
@Profile("dev")
@RequestMapping("/api/test/events")
@RequiredArgsConstructor
public class TestEventController {

    private final EventService eventService;


    // 테스트 전용: 특정 이벤트의 모든 참가자를 인증 없이 한 번에 취소합니다. 알림 발송은 수행되지 않습니다.
    @DeleteMapping("/{eventId}/participants")
    public CommonResponse<Integer> cancelAllParticipants(@PathVariable Long eventId) {
        eventService.cancelAllParticipants(eventId);
        return CommonResponse.ok(ResultCode.OK);
    }

    // 테스트 전용: 특정 이벤트에 참여 중인 사용자 목록 조회
    @GetMapping("/{eventId}/participants")
    public CommonResponse<EventParticipantsResponse> getParticipants(
            @PathVariable Long eventId) {
        EventParticipantsResponse participants = eventService.getEventParticipants(eventId);
        return CommonResponse.of(ResultCode.OK, participants);
    }

}
