package project.luckybooky.domain.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.luckybooky.domain.event.dto.request.EventRequest;
import project.luckybooky.domain.event.dto.response.EventResponse;
import project.luckybooky.domain.event.service.EventService;
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

    @Operation(summary = "이벤트 생성", description = "해당하는 값을 넣어주세요 !")
    @PostMapping
    public BaseResponse<EventResponse.EventCreateResultDTO> createEvent(
            @RequestPart EventRequest.EventCreateRequestDTO request,
            @RequestPart(required = false) MultipartFile eventImage
    ) {
        // 유저 ID 가져오기
        String userEmail = AuthenticatedUserUtils.getAuthenticatedUserEmail();
        UserResponseDTO.AllInfoDTO userInfo = authService.getUserInfo(userEmail);
        Long userId = userInfo.getId();

        Long eventId = eventService.createEvent(request, eventImage);
        return BaseResponse.onSuccess(participationService.createParticipation(userId, eventId, Boolean.TRUE));
    }

    @Operation(summary = "카테고리별 이벤트 리스트 조회", description = "조회를 희망하는 카테고리와 page&size를 넣어주세요!! <br><br>" +
            "category: 조회할 카테고리 (ex, 인기, 최신, 영화, 드라마, 스포츠, 예능, 콘서트, 그외 이벤트 중 1개) <br>" +
            "page: 조회할 페이지 번호 <br> size: 한 페이지에 조회할 이벤트 수")
    @GetMapping("/category")
    public BaseResponse<List<EventResponse.EventReadByCategoryResultDTO>> readEventListByCategory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String category
    ) {
        return BaseResponse.onSuccess(eventService.readEventListByCategory(category, page, size));
    }
}
