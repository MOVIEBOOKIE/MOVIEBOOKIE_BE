package project.luckybooky.domain.location.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.luckybooky.domain.location.converter.LocationConverter;
import project.luckybooky.domain.location.dto.request.LocationRequest;
import project.luckybooky.domain.location.dto.response.LocationResponse;
import project.luckybooky.domain.location.entity.Location;
import project.luckybooky.domain.location.repository.LocationRepository;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;

    public Location findOne(Long locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOCATION_NOT_FOUND));
    }

    public List<LocationResponse.ReadLocationsResultDTO> findLocationsByEventOptions(LocationRequest.ReadLocationsRequestDTO request) {
        List<Location> locationsByEventOptions = locationRepository.findLocationsByEventOptions(
                request.getMin(),
                request.getMax(),
                request.getMediaType(),
                request.getStartTime(),
                request.getProgressTime());

        return locationsByEventOptions.stream().map(location -> {
            Set<String> keywords = location.getLocationKeywordList().stream().map(keyword -> {
                return keyword.getDescription();
            }).collect(Collectors.toSet());
            return LocationConverter.toReadLocationsResultDTO(location, keywords);
        }).collect(Collectors.toList());
    }
}
