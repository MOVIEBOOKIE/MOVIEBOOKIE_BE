package project.luckybooky.domain.ticket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class TicketResponse {
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class ReadTicketDetailsResultDTO {
        Long ticketId;
        String title;
        String type;
        String location;
        Integer price;
        String hostName;
        Integer participants;
        String time;
        String scheduledAt;
        String address;
        String eventImageUrl;
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class ReadTicketListResultDTO {
        Long ticketId;
        String title;
        String location;
        String scheduledAt;
        String description;
        String eventImageUrl;
    }
}
