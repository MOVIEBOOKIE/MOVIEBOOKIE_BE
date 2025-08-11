package project.luckybooky.domain.location.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.luckybooky.domain.event.repository.EventRepository;
import project.luckybooky.domain.location.converter.LocationConverter;
import project.luckybooky.domain.location.dto.request.LocationRequest;
import project.luckybooky.domain.location.dto.response.LocationResponse;
import project.luckybooky.domain.location.entity.Location;
import project.luckybooky.domain.location.repository.LocationRepository;
import project.luckybooky.domain.participation.service.ParticipationService;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;

    public Location findOne(Long locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOCATION_NOT_FOUND));
    }

    /**
     * 이벤트 생성 시 옵션을 고려하여 영화관 추천
     **/
    public List<LocationResponse.ReadLocationsResultDTO> findLocationsByEventOptions(LocationRequest.ReadLocationsRequestDTO request) {
        // 이벤트 종료 시간 계산
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime startTime = LocalTime.parse(request.getStartTime(), formatter);
        LocalTime endTime = startTime.plusHours(request.getProgressTime());
        String eventEndTime = endTime.format(formatter);

        // 불가능한 영화관 리스트 조회
        List<Long> disabledList = eventRepository.findOverlappingLocationsByTime(request.getEventDate(),request.getStartTime(), eventEndTime);

        // 옵션에 만족하는 영화관 리스트 조회
        List<Location> locationsByEventOptions = locationRepository.findLocationsByEventOptions(
                request.getMin(),
                request.getMax(),
                request.getMediaType(),
                request.getStartTime(),
                request.getProgressTime(),
                disabledList.size(),
                disabledList
                );

        return locationsByEventOptions.stream().map(location -> {
            Set<String> keywords = location.getLocationKeywordList().stream().map(keyword -> {
                return keyword.getDescription();
            }).collect(Collectors.toSet());
            return LocationConverter.toReadLocationsResultDTO(location, keywords);
        }).collect(Collectors.toList());
    }
}
