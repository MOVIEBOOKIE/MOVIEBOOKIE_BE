package project.luckybooky.domain.location.converter;

import java.util.Collections;
import project.luckybooky.domain.location.dto.response.LocationResponse;
import project.luckybooky.domain.location.dto.request.LocationRequest;
import project.luckybooky.domain.location.entity.Location;

import java.util.Set;

public class LocationConverter {
    public static LocationResponse.ReadLocationsResultDTO toReadLocationsResultDTO(Location location, Set<String> keywords) {
        return LocationResponse.ReadLocationsResultDTO.builder()
                .locationId(location.getId())
                .locationName(location.getLocationName())
                .address(location.getAddress())
                .locationImageUrl(location.getLocationImageUrl())
                .locationKeywordList(keywords)
                .pricePerHour(location.getPricePerHour())
                .seatCount(location.getSeatCount())
                .hasDisabledSeat(location.getHasDisabledSeat())
                .build();
    }

    public static Location toLocation(LocationRequest.CreateLocationRequestDTO request, String locationImageUrl) {
        return Location.builder()
                .locationName(request.getLocationName())
                .address(request.getAddress())
                .locationImageUrl(locationImageUrl)
                .pricePerHour(request.getPricePerHour())
                .seatCount(request.getSeatCount())
                .hasDisabledSeat(request.getHasDisabledSeat())
                .locationKeywordList(request.getLocationKeywordList())
                .availableMediaType(request.getAvailableMediaType())
                .availableTimes(request.getAvailableTimes())
                .isStartTimeRestricted(request.getIsStartTimeRestricted())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .allowedStartTimes(
                        request.getAllowedStartTimes() == null
                                ? Collections.emptySet()
                                : request.getAllowedStartTimes()
                )
                .build();
    }

    public static LocationResponse.CreateLocationResultDTO toCreateLocationResultDTO(Location location) {
        return LocationResponse.CreateLocationResultDTO.builder()
                .locationId(location.getId())
                .locationName(location.getLocationName())
                .address(location.getAddress())
                .build();
    }
}
