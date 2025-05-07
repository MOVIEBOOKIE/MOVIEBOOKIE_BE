package project.luckybooky.domain.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.luckybooky.domain.event.dto.request.EventRequest;
import project.luckybooky.domain.event.dto.response.EventResponse;
import project.luckybooky.domain.event.service.EventService;
import project.luckybooky.global.apiPayload.common.BaseResponse;

@Tag(name = "Event", description = "이벤트 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/event")
public class EventController {
    private final EventService eventService;

    @Operation(summary = "이벤트 생성", description = "해당하는 값을 넣어주세요 !")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<EventResponse.EventCreateResultDTO> signUp(
            @RequestPart EventRequest.EventCreateRequestDTO request,
            @RequestPart MultipartFile eventImage
    ) {
        return BaseResponse.onSuccess(eventService.createEvent(request, eventImage));
    }

}
