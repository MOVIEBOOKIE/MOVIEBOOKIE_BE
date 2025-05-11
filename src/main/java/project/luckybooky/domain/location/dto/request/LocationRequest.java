package project.luckybooky.domain.location.dto.request;

import lombok.Getter;

public class LocationRequest {
    @Getter
    public static class ReadLocationsRequestDTO {
        Integer min;
        Integer max;
        String mediaType;
        String startTime;
        Integer progressTime;
    }
}
