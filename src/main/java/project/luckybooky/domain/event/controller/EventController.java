package project.luckybooky.domain.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import project.luckybooky.domain.event.dto.request.EventRequest;
import project.luckybooky.domain.event.dto.response.EventResponse;
import project.luckybooky.domain.event.service.EventService;
import project.luckybooky.domain.event.util.EventConstants;
import project.luckybooky.domain.participation.service.ParticipationService;
import project.luckybooky.global.apiPayload.response.CommonResponse;
import project.luckybooky.global.apiPayload.response.ResultCode;
import project.luckybooky.global.service.UserContextService;

@Tag(name = "Event", description = "이벤트 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class EventController {
    private final EventService eventService;
    private final ParticipationService participationService;
    private final UserContextService userContextService;

    @PostMapping(path = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "이벤트 생성", description = "multipart/form-data 로 'request' JSON 파트와 'eventImage' 파일 파트를 함께 전송")
    public CommonResponse<Long> createEvent(
            @Parameter(description = "전송할 이벤트 정보 JSON", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EventRequest.EventCreateRequestDTO.class)))
            @RequestPart(name = "request", required = true) EventRequest.EventCreateRequestDTO request,
            @Parameter(
                    description = "업로드할 이벤트 이미지",
                    required = false,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            @RequestPart(name = "eventImage", required = false)
            MultipartFile eventImage
    ) {
        Long userId = userContextService.getUserId();
        Long eventId = eventService.createEvent(userId, request, eventImage);

        return CommonResponse.of(ResultCode.CREATED, eventId);
    }


    @Operation(summary = "이벤트 신청", description = "신청하고자 하는 이벤트 ID를 넣어주세요 !")
    @PostMapping("/{eventId}/register")
    public CommonResponse<String> registerEvent(@PathVariable("eventId") Long eventId) {
        Long userId = userContextService.getUserId();

        //participationService.createParticipation(userId, eventId, Boolean.FALSE);
        eventService.registerEvent(userId, eventId);

        return CommonResponse.of(ResultCode.OK, EventConstants.REGISTER_SUCCESS.getMessage());
    }

    @Operation(summary = "이벤트 신청 취소", description = "신청 취소하고자 하는 이벤트 ID를 넣어주세요 !")
    @DeleteMapping("/{eventId}/register")
    public CommonResponse<String> cancelRegisterEvent(@PathVariable("eventId") Long eventId) {
        Long userId = userContextService.getUserId();

        participationService.deleteParticipation(userId, eventId);
        //eventService.registerEvent(userId, eventId);
        eventService.cancelEvent(userId, eventId);
        
        return CommonResponse.of(ResultCode.OK, EventConstants.REGISTER_CANCEL_SUCCESS.getMessage());
    }

    @Operation(summary = "이벤트 모집 취소", description = "모집 취소하고자 하는 이벤트 ID를 넣어주세요 !")
    @DeleteMapping("/{eventId}/recruit")
    public CommonResponse<String> cancelRecruitEvent(@PathVariable("eventId") Long eventId) {
        Long userId = userContextService.getUserId();
        String msg = eventService.cancelRecruitEvent(userId, eventId);

        return CommonResponse.of(ResultCode.OK, msg);
    }

    @Operation(summary = "이벤트 대관 신청 혹은 취소", description = "신청 혹은 취소 유무를 type에 넣어주세요! <br>" +
            "type: 0 -> 신청, 1 -> 취소")
    @PostMapping("/{eventId}/venue")
    public CommonResponse<String> venueProcess(@PathVariable("eventId") Long eventId, @RequestParam Integer type) {
        String msg = eventService.venueProcess(eventId, type);

        return CommonResponse.of(ResultCode.OK, msg);
    }

    @Operation(summary = "이벤트 대관 확정", description = "확정하고자 하는 이벤트 ID를 넣어주세요 !")
    @PostMapping("/{eventId}/venue-confirmed")
    public CommonResponse<EventResponse.EventVenueConfirmedResultDTO> venueConfirmed(
            @PathVariable("eventId") Long eventId) {
        EventResponse.EventVenueConfirmedResultDTO dto = eventService.venueConfirmed(eventId);
        return CommonResponse.of(ResultCode.OK, dto);
    }

    @Operation(summary = "상영 완료 혹은 취소", description = "상영 완료 혹은 취소 유무를 type에 넣어주세요! <br>" +
            "type: 0 -> 완료, 1 -> 취소")
    @PostMapping("/{eventId}/screening")
    public CommonResponse<String> screeningProcess(@PathVariable("eventId") Long eventId, @RequestParam Integer type) {
        Long userId = userContextService.getUserId();
        String msg = eventService.screeningProcess(userId, eventId, type);
        return CommonResponse.of(ResultCode.OK, msg);
    }

    @Operation(summary = "카테고리별 이벤트 리스트 조회", description = "조회를 희망하는 카테고리와 page&size를 넣어주세요!! <br><br>" +
            "category: 조회할 카테고리 (ex, 인기, 최신, 영화, 드라마, 스포츠, 예능, 콘서트, 기타 중 1개) <br>" +
            "page: 조회할 페이지 번호 <br> size: 한 페이지에 조회할 이벤트 수")
    @GetMapping("/category")
    public CommonResponse<EventResponse.ReadEventListWithPageResultDTO> readEventListByCategory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String category
    ) {
        EventResponse.ReadEventListWithPageResultDTO result = eventService.readEventListByCategory(category, page,
                size);

        return CommonResponse.of(ResultCode.OK, result);
    }

    @Operation(summary = "이벤트 검색", description = "검색할 content 정보와 page&size를 넣어주세요!! <br><br>" +
            "content: 이벤트 제목 (ex, 더 폴: 오디어스와 환상의 문['더 폴'만도 가능]) or 카테고리 (ex, 영화, 드라마, 스포츠, 예능, 콘서트, 기타 중 1개) <br>" +
            "page: 조회할 페이지 번호 <br> size: 한 페이지에 조회할 이벤트 수")
    @GetMapping("/search")
    public CommonResponse<EventResponse.ReadEventListWithPageResultDTO> readEventListBySearch(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String content
    ) {
        EventResponse.ReadEventListWithPageResultDTO result = eventService.readEventListBySearch(content, page, size);

        return CommonResponse.of(ResultCode.OK, result);
    }

    @Operation(summary = "이벤트 상세 조회", description = "상세 조회를 희망하는 이벤트 ID를 넣어주세요 !!")
    @GetMapping("/{eventId}")
    public CommonResponse<EventResponse.EventReadDetailsResultDTO> readEventDetails(
            @PathVariable("eventId") Long eventId) {
        Long userId = userContextService.getUserId();
        EventResponse.EventReadDetailsResultDTO dto = eventService.readEventDetails(userId, eventId);

        return CommonResponse.of(ResultCode.OK, dto);
    }

    @Operation(summary = "이벤트 상세 조회", description = "상세 조회를 희망하는 이벤트 ID를 넣어주세요 !!")
    @GetMapping("/anonymous/{eventId}")
    public CommonResponse<EventResponse.EventReadDetailsResultDTO> readEventDetailsForAnonymous(
            @PathVariable("eventId") Long eventId) {
        EventResponse.EventReadDetailsResultDTO dto = eventService.readEventDetails(null, eventId);

        return CommonResponse.of(ResultCode.OK, dto);
    }

    @Operation(summary = "홈 화면 맞춤형 콘텐츠 조회", description = "상세 조회를 희망하는 이벤트 ID를 넣어주세요 !!")
    @GetMapping("/home")
    public CommonResponse<List<EventResponse.HomeEventListResultDTO>> readHomeEventList() {
        Long userId = userContextService.getUserId();
        List<EventResponse.HomeEventListResultDTO> list = eventService.readHomeEventList(userId);

        return CommonResponse.of(ResultCode.OK, list);
    }

}
