package project.luckybooky.domain.ticket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.event.repository.EventRepository;
import project.luckybooky.domain.participation.entity.Participation;
import project.luckybooky.domain.ticket.converter.TicketConverter;
import project.luckybooky.domain.ticket.entity.Ticket;
import project.luckybooky.domain.ticket.repository.TicketRepository;
import project.luckybooky.domain.user.entity.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;

    @Transactional
    public Long createTicket(Event event) {
        List<Participation> participationList = event.getParticipationList();

        String hostName = participationList.stream()
                .filter(p -> p.getParticipateRole().equals("HOST"))
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

}
