package project.luckybooky.domain.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import project.luckybooky.domain.category.entity.Category;
import project.luckybooky.domain.category.service.CategoryService;
import project.luckybooky.domain.event.converter.EventConverter;
import project.luckybooky.domain.event.dto.request.EventRequest;
import project.luckybooky.domain.event.dto.response.EventResponse;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.event.entity.type.EventStatus;
import project.luckybooky.domain.event.repository.EventRepository;
import project.luckybooky.domain.location.entity.Location;
import project.luckybooky.domain.location.service.LocationService;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.repository.UserRepository;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;
import project.luckybooky.global.service.S3Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final LocationService locationService;
    private final CategoryService categoryService;

    @Transactional
    public Long createEvent(EventRequest.EventCreateRequestDTO request, MultipartFile eventImage) {
        String eventImageUrl = s3Service.uploadFile(eventImage);
        Category category = categoryService.findByName(request.getMediaType());
        Location location = locationService.findOne(request.getLocationId());
        String eventEndTime = toEventEndTime(request.getEventStartTime(), request.getEventProgressTime());
        Integer estimatedPrice = toEstimatedPrice(request.getEventProgressTime(), location.getPricePerHour(), request.getMinParticipants());

        Event event = EventConverter.toEvent(request, eventImageUrl, category, location, eventEndTime,estimatedPrice);
        eventRepository.save(event);
        return event.getId();
    }

    private Integer toEstimatedPrice(Integer eventProgressTime, Integer pricePerHour, Integer minParticipants) {
        int estimatedPrice = pricePerHour * eventProgressTime / minParticipants;
        return (int) (Math.round(estimatedPrice / 1000.0) * 1000);
    }

    /** 이벤트 종료 시간 생성 (시작 시각 + 진행 시간) **/
    private String toEventEndTime(String eventStartTime, Integer eventProgressTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime startTime = LocalTime.parse(eventStartTime, formatter);

        LocalTime endTime = startTime.plusHours(eventProgressTime);
        return endTime.format(formatter);
    }

    public Event findOne(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));
    }

    public List<EventResponse.EventReadByCategoryResultDTO> readEventListByCategory(String category, Integer page, Integer size) {
        Page<Event> eventList;
        switch (category) {
            case "인기":
                eventList = eventRepository.findOrderByPopularity(PageRequest.of(page, size));
                break;
            case "최신":
                eventList = eventRepository.findOrderByCreatedAtDesc(PageRequest.of(page, size));
                break;
            default:
                eventList = eventRepository.findByCategoryName(category, PageRequest.of(page, size));
                break;
        }

        return eventList.stream().map(
                e -> {
                    double percentage = ((double) e.getCurrentParticipants() / e.getMaxParticipants()) * 100;
                    int rate = Math.round((float) percentage);

                    int d_day = (int) ChronoUnit.DAYS.between(LocalDate.now(), e.getEventDate());

                    return EventConverter.toEventReadByCategoryResultDTO(e, rate, d_day);
                }
        ).collect(Collectors.toList());
    }

    public EventResponse.EventReadDetailsResultDTO readEventDetails(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));

        return EventConverter.toEventReadDetailsResultDTO(
                event,
                user.getUsername(),
                user.getHostExperienceCount(),
                formatDateRange(event.getRecruitmentStart(), event.getRecruitmentEnd()),
                "D-" + ChronoUnit.DAYS.between(LocalDate.now(), event.getEventDate()),
                Math.round((float) ((double) event.getCurrentParticipants() / event.getMaxParticipants()) * 100)
        );

    }

    /** 이벤트 모집기간 출력 포맷팅 **/
    private static String formatDateRange(LocalDate startDate, LocalDate endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy. MM. dd");

        String formattedStartDate = startDate.format(formatter);
        String formattedEndDate = endDate.format(formatter);

        return formattedStartDate + " ~ " + formattedEndDate;
    }
}
