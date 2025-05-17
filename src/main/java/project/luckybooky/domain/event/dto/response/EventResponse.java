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
    public static class ReadEventListResultDTO {
        Long eventId;
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

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class EventReadDetailsResultDTO {
        /** 이벤트 관련 **/
        Long eventId;
        String mediaType;
        String mediaTitle;
        String eventTitle;
        String description;
        Integer estimatedPrice;
        LocalDate eventDate;
        String eventTime;
        String recruitmentDate;
        String d_day;
        Integer minParticipants;
        Integer maxParticipants;
        Integer currentParticipants;
        Integer recruitmentRate;
        String posterImageUrl;
        String buttonState;

        /** 주최자 관련 **/
        String username;
        Integer recruitment;

        /** 위치 관련 **/
        String locationName;
        String address;
        String locationImageUrl;
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class EventVenueConfirmedResultDTO {
        Long ticketId;
    }
}
