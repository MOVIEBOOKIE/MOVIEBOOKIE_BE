package project.luckybooky.domain.event.dto.request;

import lombok.Getter;

import java.time.LocalDate;

public class EventRequest {

    @Getter
    public static class EventCreateRequestDTO {
        String mediaType;
        LocalDate eventDate;
        String eventStartTime;
        Integer eventProgressTime;
        LocalDate recruitmentStart;
        LocalDate recruitmentEnd;
        Integer minParticipants;
        Integer maxParticipants;
        Long locationId;
        String mediaTitle;
        String eventTitle;
        String description;
    }
}
