package project.luckybooky.domain.event.converter;

import project.luckybooky.domain.category.entity.Category;
import project.luckybooky.domain.event.dto.request.EventRequest;
import project.luckybooky.domain.event.dto.response.EventResponse;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.location.entity.Location;
import project.luckybooky.domain.user.entity.User;

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
                .isPublic(request.getIsPublic())
                .build();
    }

    public static EventResponse.EventCreateResultDTO toEventCreateResponseDTO(Event event) {
        return EventResponse.EventCreateResultDTO.builder()
                .eventId(event.getId())
                .createdAt(event.getCreatedAt())
                .build();
    }

    /** 요일 구하기 **/
    private static String getDay(LocalDate eventDate) {
        // 요일 구하기 (한글)
        DayOfWeek dayOfWeek = eventDate.getDayOfWeek();
        String[] koreanDays = {"월", "화", "수", "목", "금", "토", "일"};

        // LocalDate의 getDayOfWeek().getValue()는 1(월)~7(일)
        String day = koreanDays[dayOfWeek.getValue() - 1];

        return day;
    }

    /**
     * 날짜 포맷
     **/
    private static String getDateFormat(LocalDate eventDate) {
        String day = getDay(eventDate);

        // 날짜 포맷
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        String date = eventDate.format(formatter) + " (" + day + ")";

        return date;
    }

    public static EventResponse.ReadEventListResultDTO toEventListResultDTO(Event event) {
        // 날짜 포맷
        String eventDate = getDateFormat(event.getEventDate());

        double percentage = ((double) event.getCurrentParticipants() / event.getMaxParticipants()) * 100;
        int rate = Math.round((float) percentage);

        // d-day 계산
        Integer d_day;
        switch (event.getEventStatus()) {
            case RECRUITING -> {
                d_day = LocalDate.now().isAfter(event.getRecruitmentEnd()) ? null : (int) ChronoUnit.DAYS.between(LocalDate.now(), event.getRecruitmentEnd());
            }
            case VENUE_RESERVATION_IN_PROGRESS,VENUE_CONFIRMED -> {
                d_day = LocalDate.now().isAfter(event.getEventDate()) ? null : (int) ChronoUnit.DAYS.between(LocalDate.now(), event.getEventDate());
            }
            default -> {
                d_day = null;
            }
        }
        return EventResponse.ReadEventListResultDTO.builder()
                .eventId(event.getId())
                .mediaType(event.getCategory().getCategoryName())
                .mediaTitle(event.getMediaTitle())
                .description(event.getDescription())
                .rate(rate)
                .estimatedPrice(event.getEstimatedPrice())
                .eventDate(eventDate)
                .eventStatus(event.getEventStatus().getDescription())
                .d_day(d_day)
                .locationName(event.getLocation().getLocationName())
                .posterImageUrl(event.getPosterImageUrl())
                .build();
    }

    public static EventResponse.ReadEventListWithPageResultDTO toReadEventListWithPageResult(Integer totalPages, List<EventResponse.ReadEventListResultDTO> eventListResultDTOS) {
        return EventResponse.ReadEventListWithPageResultDTO.builder()
                .totalPages(totalPages)
                .eventList(eventListResultDTOS)
                .build();
    }

    public static EventResponse.EventReadDetailsResultDTO toEventReadDetailsResultDTO(
            Event event,
            User host,
            String userRole,
            String recruitmentDate,
            Integer recruitmentRate,
            String buttonState
    ) {
        long days = ChronoUnit.DAYS.between(LocalDate.now(), event.getRecruitmentEnd());
        String d_day = days < 0 ? null : "D-" + days;

        // 날짜 포맷
        String eventDate = getDateFormat(event.getEventDate());

        // 시간 포맷
        LocalTime localTime = LocalTime.parse(event.getEventStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
        String eventTime = localTime.format(DateTimeFormatter.ofPattern("HH시 mm분"));

        // 주최자 처리
        String username = "(탈퇴한 사용자)";
        String userImageUrl = null;
        Integer recruitment = 0;
        if (host != null) {
            username = host.getUsername();
            userImageUrl = host.getProfileImage();
            recruitment = host.getRecruitment();
        }
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

    public static EventResponse.HomeEventListResultDTO toHomeEventListResultDTO(Event event, String hostName) {
        // 날짜 포맷
        String eventDate = getDateFormat(event.getEventDate());

        return EventResponse.HomeEventListResultDTO.builder()
                .eventId(event.getId())
                .hostName(hostName)
                .eventTitle(event.getMediaTitle())
                .eventDescription(event.getDescription())
                .mediaTitle(event.getMediaTitle())
                .locationName(event.getLocation().getLocationName())
                .eventDate(eventDate)
                .posterImageUrl(event.getPosterImageUrl())
                .build();
    }

    public static EventResponse.EventVenueConfirmedResultDTO toEventVenueConfirmedResultDTO(Long ticketId) {
        return EventResponse.EventVenueConfirmedResultDTO
                .builder()
                .ticketId(ticketId)
                .build();
    }
}
