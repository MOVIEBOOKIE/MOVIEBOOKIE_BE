package project.luckybooky.domain.location.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.luckybooky.domain.location.entity.Location;
import project.luckybooky.domain.location.repository.LocationRepository;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;

    public Location findOne(Long locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOCATION_NOT_FOUND));
    }
}
