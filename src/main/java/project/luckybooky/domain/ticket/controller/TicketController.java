package project.luckybooky.domain.ticket.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.luckybooky.domain.ticket.dto.response.TicketResponse;
import project.luckybooky.domain.ticket.service.TicketService;
import project.luckybooky.global.apiPayload.response.CommonResponse;
import project.luckybooky.global.apiPayload.response.ResultCode;
import project.luckybooky.global.service.UserContextService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService ticketService;
    private final UserContextService userContextService;

    @Operation(summary = "티켓 목록 조회", description = "page&size를 넣어주세요!! <br><br>" +
            "page: 조회할 페이지 번호 <br> size: 한 페이지에 조회할 이벤트 수")
    @GetMapping
    public CommonResponse<List<TicketResponse.ReadTicketListResultDTO>> readTicketList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = userContextService.getUserId();
        List<TicketResponse.ReadTicketListResultDTO> list =
                ticketService.readTicketList(userId, page, size);
        return CommonResponse.of(ResultCode.OK, list);
    }

    @Operation(summary = "티켓 상세 조회", description = "상세 조회를 희망하는 티켓 ID를 넣어주세요 !!")
    @GetMapping("/{ticketId}")
    public CommonResponse<TicketResponse.ReadTicketDetailsResultDTO> readTicketDetails(
            @PathVariable("ticketId") Long ticketId
    ) {
        TicketResponse.ReadTicketDetailsResultDTO dto =
                ticketService.readTicketDetails(ticketId);
        return CommonResponse.of(ResultCode.OK, dto);
    }

    @Operation(summary = "티켓으로 이동", description = "이동을 희망하는 이벤트 ID를 넣어주세요 !!")
    @GetMapping("/{eventId}/to-ticket")
    public CommonResponse<TicketResponse.ReadTicketDetailsResultDTO> readTicketDetailsByEvent(
            @PathVariable("eventId") Long eventId
    ) {
        TicketResponse.ReadTicketDetailsResultDTO dto =
                ticketService.readTicketDetailsByEvent(eventId);
        return CommonResponse.of(ResultCode.OK, dto);
    }
}
