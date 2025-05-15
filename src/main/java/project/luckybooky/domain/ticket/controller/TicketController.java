package project.luckybooky.domain.ticket.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.luckybooky.domain.ticket.dto.response.TicketResponse;
import project.luckybooky.domain.ticket.service.TicketService;
import project.luckybooky.global.apiPayload.common.BaseResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService ticketService;

    @Operation(summary = "티켓 상세 조회", description = "상세 조회를 희망하는 티켓 ID를 넣어주세요 !!")
    @GetMapping("/{ticketId}")
    public BaseResponse<TicketResponse.ReadTicketDetailsResultDTO> readTicketDetails(@PathVariable("ticketId") Long ticketId) {
        return BaseResponse.onSuccess(ticketService.readTicketDetails(ticketId));
    }
}
