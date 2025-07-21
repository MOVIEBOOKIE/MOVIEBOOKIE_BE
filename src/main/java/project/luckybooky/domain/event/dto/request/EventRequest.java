package project.luckybooky.domain.event.dto.request;

import jakarta.validation.constraints.Min;
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
        @Min(value = 1, message = "최소 참여 인원은 1명 이상이어야 합니다.")
        Integer minParticipants;
        @Min(value = 1, message = "최대 참여 인원은 1명 이상이어야 합니다.")
        Integer maxParticipants;
        Long locationId;
        String mediaTitle;
        String eventTitle;
        String description;
    }
}
