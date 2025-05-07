package project.luckybooky.domain.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.luckybooky.domain.event.dto.request.EventRequest;
import project.luckybooky.domain.event.dto.response.EventResponse;
import project.luckybooky.domain.event.service.EventService;
import project.luckybooky.domain.participation.service.ParticipationService;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.global.apiPayload.common.BaseResponse;

@Tag(name = "Event", description = "이벤트 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class EventController {
    private final EventService eventService;
    private final ParticipationService participationService;

    @Operation(summary = "이벤트 생성", description = "해당하는 값을 넣어주세요 !")
    @PostMapping(value = "/{userId}")
    public BaseResponse<EventResponse.EventCreateResultDTO> createEvent(
            @RequestPart EventRequest.EventCreateRequestDTO request,
            @RequestPart(required = false) MultipartFile eventImage,
            @PathVariable Long userId
            ) {
        Long eventId = eventService.createEvent(request, eventImage);
        return BaseResponse.onSuccess(participationService.createParticipation(userId, eventId, Boolean.TRUE));
    }

}
