package project.luckybooky.domain.participation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.luckybooky.domain.event.dto.response.EventResponse;
import project.luckybooky.domain.participation.service.ParticipationService;
import project.luckybooky.global.apiPayload.response.CommonResponse;
import project.luckybooky.global.apiPayload.response.ResultCode;
import project.luckybooky.global.service.UserContextService;

@Tag(name = "Participation", description = "이벤트 참여자 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/participation")
public class ParticipationController {
    private final ParticipationService participationService;
    private final UserContextService userContextService;

    @Operation(summary = "내가 신청한 이벤트 리스트 조회 (신청, 확정)", description = "type과 page&size를 넣어주세요!! <br><br>" +
            "type: 0 -> 신청, 1 -> 확정 <br>" +
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

    @Operation(summary = "내가 만든 이벤트 리스트 조회 (모집, 확정)", description = "type과 page&size를 넣어주세요!! <br><br>" +
            "type: 0 -> 모집, 1 -> 확정 <br>" +
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
}
