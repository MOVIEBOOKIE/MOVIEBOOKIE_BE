package project.luckybooky.domain.event.converter;

import project.luckybooky.domain.category.entity.Category;
import project.luckybooky.domain.event.dto.request.EventRequest;
import project.luckybooky.domain.event.dto.response.EventResponse;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.location.entity.Location;

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
        return EventResponse.ReadEventListResultDTO.builder()
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
            Integer recruitment,
            String recruitmentDate,
            String d_day,
            Integer recruitmentRate
    ) {
        return EventResponse.EventReadDetailsResultDTO.builder()
                .mediaType(event.getCategory().getCategoryName())
                .mediaTitle(event.getMediaTitle())
                .eventTime(event.getEventTitle())
                .eventTitle(event.getEventTitle())
                .description(event.getDescription())
                .estimatedPrice(event.getEstimatedPrice())
                .eventDate(event.getEventDate())
                .eventTime(event.getEventStartTime())
                .recruitmentDate(recruitmentDate)
                .d_day(d_day)
                .minParticipants(event.getMinParticipants())
                .maxParticipants(event.getMaxParticipants())
                .currentParticipants(event.getCurrentParticipants())
                .recruitmentRate(recruitmentRate)
                .posterImageUrl(event.getPosterImageUrl())
                .username(username)
                .recruitment(recruitment)
                .locationName(event.getLocation().getLocationName())
                .address(event.getLocation().getAddress())
                .locationImageUrl(event.getLocation().getLocationImageUrl())
                .build();
    }
}
