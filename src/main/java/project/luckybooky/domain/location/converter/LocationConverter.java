package project.luckybooky.domain.location.converter;

import project.luckybooky.domain.event.dto.response.EventResponse;
import project.luckybooky.domain.location.dto.response.LocationResponse;
import project.luckybooky.domain.location.entity.Location;

import java.util.Set;

public class LocationConverter {
    public static LocationResponse.ReadLocationsResultDTO toReadLocationsResultDTO(Location location, Set<String> keywords) {
        return LocationResponse.ReadLocationsResultDTO.builder()
                .locationName(location.getLocationName())
                .address(location.getAddress())
                .locationImageUrl(location.getLocationImageUrl())
                .locationKeywordList(keywords)
                .pricePerHour(location.getPricePerHour())
                .seatCount(location.getSeatCount())
                .hasDisabledSeat(location.getHasDisabledSeat())
                .build();
    }
}
