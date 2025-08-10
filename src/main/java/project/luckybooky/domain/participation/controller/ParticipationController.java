package project.luckybooky.domain.participation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import project.luckybooky.domain.event.dto.response.EventResponse;
import project.luckybooky.domain.participation.service.ParticipationService;
import project.luckybooky.global.apiPayload.response.CommonResponse;
import project.luckybooky.global.apiPayload.response.ResultCode;
import project.luckybooky.global.service.UserContextService;

@Tag(name = "Participation", description = "이벤트 참여 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/participation")
public class ParticipationController {
    private final ParticipationService participationService;
    private final UserContextService userContextService;

    @Operation(summary = "내가 신청한 이벤트 리스트 조회 (신청, 확정, 취소)", description = "type과 page&size를 넣어주세요!! <br><br>" +
            "type: 0 -> 신청, 1 -> 확정, 2 -> 취소 <br>" +
            "page: 조회할 페이지 번호 <br> size: 한 페이지에 조회할 이벤트 수")
    @GetMapping("/registered")
    public CommonResponse<List<EventResponse.ReadEventListResultDTO>> readRegisteredEventList(
            @RequestParam int type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = userContextService.getUserId();
        List<EventResponse.ReadEventListResultDTO> list =
                participationService.readEventList(userId, type, 0, page, size);
        return CommonResponse.of(ResultCode.OK, list);
    }

    @Operation(summary = "내가 만든 이벤트 리스트 조회 (모집, 확정, 취소)", description = "type과 page&size를 넣어주세요!! <br><br>" +
            "type: 0 -> 모집, 1 -> 확정, 2 -> 취소 <br>" +
            "page: 조회할 페이지 번호 <br> size: 한 페이지에 조회할 이벤트 수")
    @GetMapping("/hosted")
    public CommonResponse<List<EventResponse.ReadEventListResultDTO>> readHostedEventList(
            @RequestParam int type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = userContextService.getUserId();
        List<EventResponse.ReadEventListResultDTO> list =
                participationService.readEventList(userId, type, 1, page, size);
        return CommonResponse.of(ResultCode.OK, list);
    }

    @Operation(summary = "선택된 날짜에 이벤트 모집 가능 여부 조회", description = "선택된 날짜를 date에 아래 형식대로 넣어주세요! <br>" +
            "date: 2025-05-24")
    @GetMapping("/recruitable")
    public CommonResponse<String> venueProcess(@RequestParam String date) {
        Long userId = userContextService.getUserId();
        String result = participationService.isRecruitableOnDate(userId, date);
        return CommonResponse.of(ResultCode.OK, result);
    }
}
