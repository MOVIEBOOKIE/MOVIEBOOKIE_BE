package project.luckybooky.domain.admin.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import project.luckybooky.domain.admin.dto.AdminEventParticipantsResponse;
import project.luckybooky.domain.admin.dto.AdminEventUpdateRequest;
import project.luckybooky.domain.admin.dto.AdminEventUpdateResponse;
import project.luckybooky.domain.event.dto.response.EventResponse;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.event.entity.type.EventStatus;
import project.luckybooky.domain.event.repository.EventRepository;
import project.luckybooky.domain.event.service.EventService;
import project.luckybooky.domain.location.entity.Location;
import project.luckybooky.domain.location.service.LocationService;
import project.luckybooky.domain.participation.dto.ParticipantInfoDto;
import project.luckybooky.domain.participation.service.ParticipantInfoService;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@Service
@RequiredArgsConstructor
public class AdminEventManagementService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final EventService eventService;
    private final EventRepository eventRepository;
    private final ParticipantInfoService participantInfoService;
    private final LocationService locationService;

    @Transactional
    public EventResponse.EventVenueConfirmedResultDTO confirmVenue(Long eventId) {
        return eventService.venueConfirmed(eventId);
    }

    @Transactional(readOnly = true)
    public AdminEventParticipantsResponse getParticipants(Long eventId) {
        eventService.findOne(eventId);
        List<ParticipantInfoDto> participants = participantInfoService.getParticipantInfo(eventId);
        return AdminEventParticipantsResponse.builder()
                .participants(participants)
                .totalParticipantsCount(participants.size())
                .build();
    }

    @Transactional
    public AdminEventUpdateResponse updateEvent(Long eventId, AdminEventUpdateRequest request) {
        Event event = eventService.findOne(eventId);
        validateEditableStatus(event);
        validateHasAnyUpdatableField(request);

        String eventTitle = StringUtils.hasText(request.getEventTitle()) ? request.getEventTitle() : event.getEventTitle();
        String description = request.getDescription() != null ? request.getDescription() : event.getDescription();
        LocalDate eventDate = request.getEventDate() != null ? request.getEventDate() : event.getEventDate();
        String eventStartTime = StringUtils.hasText(request.getEventStartTime()) ? request.getEventStartTime() : event.getEventStartTime();
        int eventProgressTime = request.getEventProgressTime() != null ? request.getEventProgressTime() : getCurrentProgressTime(event);

        if (eventProgressTime <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        String eventEndTime = toEventEndTime(eventStartTime, eventProgressTime);
        Location location = request.getLocationId() != null ? locationService.findOne(request.getLocationId()) : event.getLocation();
        Integer minParticipants = request.getMinParticipants() != null ? request.getMinParticipants() : event.getMinParticipants();
        Integer maxParticipants = request.getMaxParticipants() != null ? request.getMaxParticipants() : event.getMaxParticipants();
        String posterImageUrl = StringUtils.hasText(request.getPosterImageUrl()) ? request.getPosterImageUrl() : event.getPosterImageUrl();

        validateParticipantCount(event, minParticipants, maxParticipants);
        validateLocationAvailability(eventId, location.getId(), eventDate, eventStartTime, eventEndTime);

        Integer estimatedPrice = toEstimatedPrice(eventProgressTime, location.getPricePerHour(), minParticipants);

        event.updateByAdmin(
                eventTitle,
                description,
                eventDate,
                eventStartTime,
                eventEndTime,
                location,
                minParticipants,
                maxParticipants,
                posterImageUrl,
                estimatedPrice
        );

        return AdminEventUpdateResponse.builder()
                .eventId(event.getId())
                .eventTitle(event.getEventTitle())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .eventStartTime(event.getEventStartTime())
                .eventEndTime(event.getEventEndTime())
                .locationId(event.getLocation().getId())
                .minParticipants(event.getMinParticipants())
                .maxParticipants(event.getMaxParticipants())
                .posterImageUrl(event.getPosterImageUrl())
                .estimatedPrice(event.getEstimatedPrice())
                .build();
    }

    private void validateEditableStatus(Event event) {
        if (event.getEventStatus() == EventStatus.COMPLETED || event.getEventStatus() == EventStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

    private void validateHasAnyUpdatableField(AdminEventUpdateRequest request) {
        if (request.getEventTitle() == null
                && request.getDescription() == null
                && request.getEventDate() == null
                && request.getEventStartTime() == null
                && request.getEventProgressTime() == null
                && request.getLocationId() == null
                && request.getMinParticipants() == null
                && request.getMaxParticipants() == null
                && request.getPosterImageUrl() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

    private void validateParticipantCount(Event event, Integer minParticipants, Integer maxParticipants) {
        if (minParticipants > maxParticipants) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (event.getCurrentParticipants() > maxParticipants) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

    private void validateLocationAvailability(
            Long eventId,
            Long locationId,
            LocalDate eventDate,
            String eventStartTime,
            String eventEndTime
    ) {
        Integer overlapCount = eventRepository.isExistOverlappingLocationsByTimeExcludeEvent(
                eventId,
                locationId,
                eventDate,
                eventStartTime,
                eventEndTime
        );
        if (overlapCount != null && overlapCount > 0) {
            throw new BusinessException(ErrorCode.LOCATION_ALREADY_RESERVED);
        }
    }

    private String toEventEndTime(String eventStartTime, Integer eventProgressTime) {
        LocalTime startTime = LocalTime.parse(eventStartTime, TIME_FORMATTER);
        LocalTime endTime = startTime.plusHours(eventProgressTime);
        return endTime.format(TIME_FORMATTER);
    }

    private int getCurrentProgressTime(Event event) {
        LocalTime start = LocalTime.parse(event.getEventStartTime(), TIME_FORMATTER);
        LocalTime end = LocalTime.parse(event.getEventEndTime(), TIME_FORMATTER);
        return (int) java.time.temporal.ChronoUnit.HOURS.between(start, end);
    }

    private Integer toEstimatedPrice(Integer eventProgressTime, Integer pricePerHour, Integer minParticipants) {
        int estimatedPrice = pricePerHour * eventProgressTime / minParticipants;
        return (int) (Math.round(estimatedPrice / 1000.0) * 1000);
    }
}
