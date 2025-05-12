package project.luckybooky.domain.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.luckybooky.domain.event.dto.request.EventRequest;
import project.luckybooky.domain.event.dto.response.EventResponse;
import project.luckybooky.domain.event.service.EventService;
import project.luckybooky.domain.event.util.EventConstants;
import project.luckybooky.domain.participation.service.ParticipationService;
import project.luckybooky.domain.user.dto.response.UserResponseDTO;
import project.luckybooky.domain.user.service.AuthService;
import project.luckybooky.domain.user.util.AuthenticatedUserUtils;
import project.luckybooky.global.apiPayload.common.BaseResponse;

import java.util.List;

@Tag(name = "Event", description = "이벤트 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class EventController {
    private final EventService eventService;
    private final ParticipationService participationService;
    private final AuthService authService;

    /** 로그인 정보로부터 유저 ID 추출 **/
    private Long toUserId() {
        String userEmail = AuthenticatedUserUtils.getAuthenticatedUserEmail();
        UserResponseDTO.AllInfoDTO userInfo = authService.getUserInfo(userEmail);
        return userInfo.getId();
    }

    @Operation(summary = "이벤트 생성", description = "해당하는 값을 넣어주세요 !")
    @PostMapping
    public BaseResponse<EventResponse.EventCreateResultDTO> createEvent(
            @RequestPart EventRequest.EventCreateRequestDTO request,
            @RequestPart(required = false) MultipartFile eventImage
    ) {
        // 유저 ID 가져오기
        Long userId = toUserId();

        Long eventId = eventService.createEvent(request, eventImage);
        return BaseResponse.onSuccess(participationService.createParticipation(userId, eventId, Boolean.TRUE));
    }

    @Operation(summary = "이벤트 신청", description = "신청하고자 하는 이벤트 ID를 넣어주세요 !")
    @PostMapping("/{eventId}/register")
    public BaseResponse<String> registerEvent(@PathVariable("eventId") Long eventId) {
        // 유저 ID 가져오기
        Long userId = toUserId();

        participationService.createParticipation(userId, eventId, Boolean.FALSE);
        eventService.registerEvent(eventId, Boolean.TRUE);

        return BaseResponse.onSuccess(EventConstants.REGISTER_SUCCESS.getMessage());
    }

    @Operation(summary = "이벤트 신청 취소", description = "신청 취소하고자 하는 이벤트 ID를 넣어주세요 !")
    @DeleteMapping("/{eventId}/register")
    public BaseResponse<String> cancelRegisterEvent(@PathVariable("eventId") Long eventId) {
        // 유저 ID 가져오기
        Long userId = toUserId();

        participationService.deleteParticipation(userId, eventId);
        eventService.registerEvent(eventId,Boolean.FALSE);

        return BaseResponse.onSuccess(EventConstants.REGISTER_CANCEL_SUCCESS.getMessage());
    }

    @Operation(summary = "카테고리별 이벤트 리스트 조회", description = "조회를 희망하는 카테고리와 page&size를 넣어주세요!! <br><br>" +
            "category: 조회할 카테고리 (ex, 인기, 최신, 영화, 드라마, 스포츠, 예능, 콘서트, 기타 중 1개) <br>" +
            "page: 조회할 페이지 번호 <br> size: 한 페이지에 조회할 이벤트 수")
    @GetMapping("/category")
    public BaseResponse<List<EventResponse.EventReadByCategoryResultDTO>> readEventListByCategory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String category
    ) {
        return BaseResponse.onSuccess(eventService.readEventListByCategory(category, page, size));
    }

    @Operation(summary = "이벤트 상세 조회", description = "상세 조회를 희망하는 이벤트 ID를 넣어주세요 !!")
    @GetMapping("/{eventId}")
    public BaseResponse<EventResponse.EventReadDetailsResultDTO> readEventDetails(@PathVariable("eventId") Long eventId) {
        Long userId = toUserId();
        return BaseResponse.onSuccess(eventService.readEventDetails(userId, eventId));
    }
}
