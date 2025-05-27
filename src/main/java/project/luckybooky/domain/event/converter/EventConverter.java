package project.luckybooky.domain.event.converter;

import project.luckybooky.domain.category.entity.Category;
import project.luckybooky.domain.event.dto.request.EventRequest;
import project.luckybooky.domain.event.dto.response.EventResponse;
import project.luckybooky.domain.event.dto.response.EventResponse.EventCreateResultDTO;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.location.entity.Location;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class EventConverter {
    public static Event toEvent(
            EventRequest.EventCreateRequestDTO request,
            String eventImageUrl,
            Category category,
            Location location,
            String eventEndTime,
            Integer estimatedPrice
    ) {
        return Event.builder()
                .location(location)
                .category(category)
                .mediaTitle(request.getMediaTitle())
                .eventTitle(request.getEventTitle())
                .description(request.getDescription())
                .eventDate(request.getEventDate())
                .eventStartTime(request.getEventStartTime())
                .eventEndTime(eventEndTime)
                .recruitmentStart(request.getRecruitmentStart())
                .recruitmentEnd(request.getRecruitmentEnd())
                .estimatedPrice(estimatedPrice)
                .posterImageUrl(eventImageUrl)
                .minParticipants(request.getMinParticipants())
                .maxParticipants(request.getMaxParticipants())
                .build();
    }

    public static EventResponse.EventCreateResultDTO toEventCreateResponseDTO(Event event) {
        return EventResponse.EventCreateResultDTO.builder()
                .eventId(event.getId())
                .createdAt(event.getCreatedAt())
                .build();
    }

    public static EventResponse.ReadEventListResultDTO toEventListResultDTO(Event event, Integer rate, Integer d_day) {
        d_day = (d_day == -1) ? null : d_day;
        return EventResponse.ReadEventListResultDTO.builder()
                .eventId(event.getId())
                .mediaType(event.getCategory().getCategoryName())
                .mediaTitle(event.getMediaTitle())
                .description(event.getDescription())
                .rate(rate)
                .estimatedPrice(event.getEstimatedPrice())
                .eventDate(event.getEventDate())
                .eventStatus(event.getEventStatus().getDescription())
                .d_day(d_day)
                .locationName(event.getLocation().getLocationName())
                .posterImageUrl(event.getPosterImageUrl())
                .build();
    }

    public static EventResponse.EventReadDetailsResultDTO toEventReadDetailsResultDTO(
            Event event,
            String username,
            String userImageUrl,
            Integer recruitment,
            String userRole,
            String recruitmentDate,
            Integer recruitmentRate,
            String buttonState
    ) {
        long days = ChronoUnit.DAYS.between(LocalDate.now(), event.getRecruitmentEnd());
        String d_day = days < 0 ? null : "D-" + days;

        // 요일 구하기 (한글)
        DayOfWeek dayOfWeek = event.getEventDate().getDayOfWeek();
        String[] koreanDays = {"월", "화", "수", "목", "금", "토", "일"};

        // LocalDate의 getDayOfWeek().getValue()는 1(월)~7(일)
        String day = koreanDays[dayOfWeek.getValue() - 1];

        // 날짜 포맷
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        String eventDate = event.getEventDate().format(formatter) + " (" + day + ")";

        // 시간 포맷
        LocalTime localTime = LocalTime.parse(event.getEventStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
        String eventTime = localTime.format(DateTimeFormatter.ofPattern("HH시 mm분"));

        return EventResponse.EventReadDetailsResultDTO.builder()
                .eventId(event.getId())
                .mediaType(event.getCategory().getCategoryName())
                .mediaTitle(event.getMediaTitle())
                .eventTime(event.getEventTitle())
                .eventTitle(event.getEventTitle())
                .description(event.getDescription())
                .estimatedPrice(event.getEstimatedPrice())
                .eventDate(eventDate)
                .eventTime(eventTime)
                .recruitmentDate(recruitmentDate)
                .d_day(d_day)
                .minParticipants(event.getMinParticipants())
                .maxParticipants(event.getMaxParticipants())
                .currentParticipants(event.getCurrentParticipants())
                .recruitmentRate(recruitmentRate)
                .posterImageUrl(event.getPosterImageUrl())
                .buttonState(buttonState)
                .eventState(event.getEventStatus().getDescription())
                .userRole(userRole)
                .username(username)
                .userImageUrl(userImageUrl)
                .recruitment(recruitment)
                .locationName(event.getLocation().getLocationName())
                .address(event.getLocation().getAddress())
                .locationImageUrl(event.getLocation().getLocationImageUrl())
                .latitude(event.getLocation().getLatitude())
                .longitude(event.getLocation().getLongitude())
                .build();
    }

    public static EventResponse.EventVenueConfirmedResultDTO toEventVenueConfirmedResultDTO(Long ticketId) {
        return EventResponse.EventVenueConfirmedResultDTO
                .builder()
                .ticketId(ticketId)
                .build();
    }

    public static EventResponse.HomeEventListResultDTO toHomeEventListResultDTO(Event event, String eventDate) {
        return EventResponse.HomeEventListResultDTO.builder()
                .eventId(event.getId())
                .type(event.getCategory().getCategoryName())
                .title(event.getMediaTitle())
                .eventStatus(event.getEventStatus().getDescription())
                .eventDate(eventDate)
                .locationName(event.getLocation().getLocationName())
                .posterImageUrl(event.getPosterImageUrl())
                .build();
    }

    public static EventCreateResultDTO toCreateResult(Long eventId) {
        return EventCreateResultDTO.builder()
                .eventId(eventId)
                .build();
    }

    public static EventResponse.ReadEventListByCategoryResultDTO toReadEventListByCategoryResult(Integer totalPages, List<EventResponse.ReadEventListResultDTO> eventListResultDTOS) {
        return EventResponse.ReadEventListByCategoryResultDTO.builder()
                .totalPages(totalPages)
                .eventList(eventListResultDTOS)
                .build();
    }
}
