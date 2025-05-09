package project.luckybooky.domain.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

}
