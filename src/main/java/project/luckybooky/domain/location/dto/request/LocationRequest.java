package project.luckybooky.domain.location.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.luckybooky.domain.location.entity.type.AvailableMediaType;
import project.luckybooky.domain.location.entity.type.LocationKeyword;

import java.time.LocalDate;
import java.util.Set;

public class LocationRequest {
    @Getter
    public static class ReadLocationsRequestDTO {
        Integer min;
        Integer max;
        String mediaType;
        String startTime;
        Integer progressTime;
        LocalDate eventDate;
    }

    @Getter
    @NoArgsConstructor
    public static class CreateLocationRequestDTO {
        @NotBlank(message = "장소명은 필수입니다.")
        private String locationName;

        @NotBlank(message = "주소는 필수입니다.")
        private String address;

        private String locationImageUrl;

        @NotNull(message = "시간당 가격은 필수입니다.")
        @Min(value = 0, message = "시간당 가격은 0 이상이어야 합니다.")
        private Integer pricePerHour;

        @NotNull(message = "좌석 수는 필수입니다.")
        @Min(value = 1, message = "좌석 수는 1 이상이어야 합니다.")
        private Integer seatCount;

        @NotNull(message = "장애인석 보유 여부는 필수입니다.")
        private Boolean hasDisabledSeat;

        @NotEmpty(message = "장소 키워드는 최소 1개 이상 필요합니다.")
        private Set<LocationKeyword> locationKeywordList;

        @NotNull(message = "지원 가능한 미디어 타입은 필수입니다.")
        private AvailableMediaType availableMediaType;

        @NotNull(message = "진행 가능한 러닝타임은 필수입니다.")
        @Min(value = 0, message = "진행 가능한 러닝타임은 0 이상이어야 합니다.")
        private Integer availableTimes;

        @NotNull(message = "시작 시간 제한 여부는 필수입니다.")
        private Boolean isStartTimeRestricted;

        @NotNull(message = "위도는 필수입니다.")
        @DecimalMin(value = "-90.0", message = "위도 범위가 올바르지 않습니다.")
        @DecimalMax(value = "90.0", message = "위도 범위가 올바르지 않습니다.")
        private Double latitude;

        @NotNull(message = "경도는 필수입니다.")
        @DecimalMin(value = "-180.0", message = "경도 범위가 올바르지 않습니다.")
        @DecimalMax(value = "180.0", message = "경도 범위가 올바르지 않습니다.")
        private Double longitude;

        private Set<String> allowedStartTimes;
    }
}
