package project.luckybooky.domain.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            ) 
    {
        // 유저 ID 가져오기
        String userEmail = AuthenticatedUserUtils.getAuthenticatedUserEmail();
        UserResponseDTO.AllInfoDTO userInfo = authService.getUserInfo(userEmail);
        Long userId = userInfo.getId();

        Long eventId = eventService.createEvent(request, eventImage);
        return BaseResponse.onSuccess(participationService.createParticipation(userId, eventId, Boolean.TRUE));
    }

}
