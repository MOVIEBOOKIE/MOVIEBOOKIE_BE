package project.luckybooky.domain.ticket.converter;

import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.ticket.dto.response.TicketResponse;
import project.luckybooky.domain.ticket.entity.Ticket;
import project.luckybooky.domain.user.entity.User;

import java.util.List;

public class TicketConverter {
    public static Ticket toTicket(Event event, List<User> userList, String scheduledAt, String time, String hostName) {
        return Ticket.builder()
                .title(event.getMediaTitle())
                .type(event.getCategory().getCategoryName())
                .location(event.getLocation().getLocationName())
                .address(event.getLocation().getAddress())
                .scheduledAt(scheduledAt)
                .time(time)
                .price(event.getEstimatedPrice())
                .hostName(hostName)
                .eventImageUrl(event.getPosterImageUrl())
                .participants(event.getCurrentParticipants())
                .userList(userList)
                .build();
    }

    public static TicketResponse.ReadTicketDetailsResultDTO toReadTicketDetailsResultDTO(Ticket ticket) {
        return TicketResponse.ReadTicketDetailsResultDTO.builder()
                .ticketId(ticket.getId())
                .title(ticket.getTitle())
                .type(ticket.getType())
                .location(ticket.getLocation())
                .price(ticket.getPrice())
                .hostName(ticket.getHostName())
                .participants(ticket.getParticipants())
                .time(ticket.getTime())
                .scheduledAt(ticket.getScheduledAt())
                .address(ticket.getAddress())
                .build();
    }
}
