package project.luckybooky.domain.ticket.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import project.luckybooky.domain.ticket.dto.response.TicketResponse;
import project.luckybooky.domain.ticket.service.TicketService;
import project.luckybooky.global.apiPayload.common.BaseResponse;
import project.luckybooky.global.service.UserContextService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService ticketService;
    private final UserContextService userContextService;

    @Operation(summary = "티켓 목록 조회", description = "page&size를 넣어주세요!! <br><br>" +
            "page: 조회할 페이지 번호 <br> size: 한 페이지에 조회할 이벤트 수")
    @GetMapping
    public BaseResponse<List<TicketResponse.ReadTicketListResultDTO>> readTicketList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = userContextService.getUserId();
        return BaseResponse.onSuccess(ticketService.readTicketList(userId, page, size));
    }

    @Operation(summary = "티켓 상세 조회", description = "상세 조회를 희망하는 티켓 ID를 넣어주세요 !!")
    @GetMapping("/{ticketId}")
    public BaseResponse<TicketResponse.ReadTicketDetailsResultDTO> readTicketDetails(@PathVariable("ticketId") Long ticketId) {
        return BaseResponse.onSuccess(ticketService.readTicketDetails(ticketId));
    }
}
