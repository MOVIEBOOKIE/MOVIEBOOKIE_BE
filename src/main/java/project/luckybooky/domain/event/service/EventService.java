package project.luckybooky.domain.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import project.luckybooky.domain.category.entity.Category;
import project.luckybooky.domain.category.service.CategoryService;
import project.luckybooky.domain.event.converter.EventConverter;
import project.luckybooky.domain.event.dto.request.EventRequest;
import project.luckybooky.domain.event.dto.response.EventResponse;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.event.repository.EventRepository;
import project.luckybooky.domain.location.entity.Location;
import project.luckybooky.domain.location.service.LocationService;
import project.luckybooky.global.service.S3Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final S3Service s3Service;
    private final LocationService locationService;
    private final CategoryService categoryService;

    @Transactional
    public EventResponse.EventCreateResultDTO createEvent(EventRequest.EventCreateRequestDTO request, MultipartFile eventImage) {
        String eventImageUrl = s3Service.uploadFile(eventImage);
        Category category = categoryService.findByName(request.getMediaType());
        Location location = locationService.findOne(request.getLocationId());
        String eventEndTime = toEventEndTime(request.getEventStartTime(), request.getEventProgressTime());

        Event event = EventConverter.toEvent(request, eventImageUrl, category, location, eventEndTime);
        eventRepository.save(event);
        return EventConverter.toEventCreateResponseDTO(event);
    }

    /** 이벤트 종료 시간 생성 (시작 시각 + 진행 시간) **/
    private String toEventEndTime(String eventStartTime, Integer eventProgressTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime startTime = LocalTime.parse(eventStartTime, formatter);

        LocalTime endTime = startTime.plusHours(eventProgressTime);
        return endTime.format(formatter);
    }
}
