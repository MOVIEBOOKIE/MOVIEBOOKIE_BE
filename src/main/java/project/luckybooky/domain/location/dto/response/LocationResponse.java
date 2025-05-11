package project.luckybooky.domain.location.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

public class LocationResponse {
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class ReadLocationsResultDTO {
        String locationName;
        String address;
        String locationImageUrl;
        Set<String> locationKeywordList;
        Integer seatCount;
        Boolean hasDisabledSeat;
    }
}
