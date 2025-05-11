package project.luckybooky.domain.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class EventResponse {
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class EventCreateResultDTO {
        Long eventId;
        LocalDateTime createdAt;
    }


    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class EventReadByCategoryResultDTO {
        String mediaType;
        String mediaTitle;
        String description;
        Integer rate;
        Integer estimatedPrice;
        LocalDate eventDate;
        String eventStatus;
        Integer d_day;
        String locationName;
        String posterImageUrl;
    }
}
