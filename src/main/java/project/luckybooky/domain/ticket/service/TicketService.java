package project.luckybooky.domain.ticket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.participation.entity.Participation;
import project.luckybooky.domain.participation.entity.type.ParticipateRole;
import project.luckybooky.domain.ticket.converter.TicketConverter;
import project.luckybooky.domain.ticket.dto.response.TicketResponse;
import project.luckybooky.domain.ticket.entity.Ticket;
import project.luckybooky.domain.ticket.repository.TicketRepository;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketService {
    private final TicketRepository ticketRepository;

    @Transactional
    public Long createTicket(Event event) {
        List<Participation> participationList = event.getParticipationList();

        String hostName = participationList.stream()
                .filter(p -> p.getParticipateRole().equals(ParticipateRole.HOST))
                .findFirst()
                .orElse(null)
                .getUser()
                .getUsername();

        List<User> userList = participationList.stream().map(participation -> {
            return participation.getUser();
        }).collect(Collectors.toList());

        String scheduledAt = event.getEventDate().format(DateTimeFormatter.ofPattern("yyyy. MM. dd"));
        String time = event.getEventStartTime() + "~" + event.getEventEndTime();

        Ticket ticket = TicketConverter.toTicket(event, userList, scheduledAt, time, hostName);
        ticketRepository.save(ticket);

        return ticket.getId();
    }

    public TicketResponse.ReadTicketDetailsResultDTO readTicketDetails(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
        return TicketConverter.toReadTicketDetailsResultDTO(ticket);
    }

    public List<TicketResponse.ReadTicketListResultDTO> readTicketList(Long userId, Integer page, Integer size) {
        Page<Ticket> ticketList = ticketRepository.findTicketsByUserId(userId, PageRequest.of(page, size));

        return ticketList.stream().map(ticket -> {
            return TicketConverter.toReadTicketListResultDTO(ticket);
        }).collect(Collectors.toList());
    }
}
