package project.luckybooky.domain.location.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import project.luckybooky.domain.event.repository.EventRepository;
import project.luckybooky.domain.location.dto.request.LocationRequest;
import project.luckybooky.domain.location.dto.response.LocationResponse;
import project.luckybooky.domain.location.entity.Location;
import project.luckybooky.domain.location.entity.type.AvailableMediaType;
import project.luckybooky.domain.location.entity.type.LocationKeyword;
import project.luckybooky.domain.location.repository.LocationRepository;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private EventRepository eventRepository;

    private LocationService locationService;

    @BeforeEach
    void setUp() {
        locationService = new LocationService(locationRepository, eventRepository);
    }

    @Test
    @DisplayName("장소 등록 성공 - 시작 시간 제한이 없으면 allowedStartTimes는 빈 값으로 저장된다")
    void createLocation_success_withoutStartTimeRestriction() throws Exception {
        LocationRequest.CreateLocationRequestDTO request = new LocationRequest.CreateLocationRequestDTO();
        setField(request, "locationName", "테스트 상영관");
        setField(request, "address", "서울시 테스트구");
        setField(request, "locationImageUrl", "https://example.com/location.png");
        setField(request, "pricePerHour", 10000);
        setField(request, "seatCount", 12);
        setField(request, "hasDisabledSeat", true);
        setField(request, "locationKeywordList", Set.of(LocationKeyword.PRIVATE, LocationKeyword.COZINESS));
        setField(request, "availableMediaType", AvailableMediaType.ALL);
        setField(request, "availableTimes", 3);
        setField(request, "isStartTimeRestricted", false);
        setField(request, "latitude", 37.5d);
        setField(request, "longitude", 127.0d);
        setField(request, "allowedStartTimes", null);

        when(locationRepository.save(any(Location.class))).thenAnswer(invocation -> {
            Location location = invocation.getArgument(0, Location.class);
            setField(location, "id", 101L);
            return location;
        });

        LocationResponse.CreateLocationResultDTO result = locationService.createLocation(request);

        assertThat(result.getLocationId()).isEqualTo(101L);
        assertThat(result.getLocationName()).isEqualTo("테스트 상영관");
        assertThat(result.getAddress()).isEqualTo("서울시 테스트구");
    }

    @Test
    @DisplayName("장소 등록 실패 - 시작 시간 제한인데 allowedStartTimes가 비어 있으면 예외")
    void createLocation_fail_whenRestrictedWithoutAllowedStartTimes() throws Exception {
        LocationRequest.CreateLocationRequestDTO request = baseRequest();
        setField(request, "isStartTimeRestricted", true);
        setField(request, "allowedStartTimes", Set.of());

        assertThatThrownBy(() -> locationService.createLocation(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.LOCATION_ALLOWED_START_TIMES_REQUIRED);
    }

    @Test
    @DisplayName("장소 등록 실패 - 시작 시간 제한이 없는데 allowedStartTimes가 있으면 예외")
    void createLocation_fail_whenNotRestrictedWithAllowedStartTimes() throws Exception {
        LocationRequest.CreateLocationRequestDTO request = baseRequest();
        setField(request, "isStartTimeRestricted", false);
        setField(request, "allowedStartTimes", Set.of("10:00"));

        assertThatThrownBy(() -> locationService.createLocation(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.LOCATION_ALLOWED_START_TIMES_MUST_BE_EMPTY);
    }

    private LocationRequest.CreateLocationRequestDTO baseRequest() throws Exception {
        LocationRequest.CreateLocationRequestDTO request = new LocationRequest.CreateLocationRequestDTO();
        setField(request, "locationName", "테스트 상영관");
        setField(request, "address", "서울시 테스트구");
        setField(request, "locationImageUrl", "https://example.com/location.png");
        setField(request, "pricePerHour", 10000);
        setField(request, "seatCount", 12);
        setField(request, "hasDisabledSeat", true);
        setField(request, "locationKeywordList", Set.of(LocationKeyword.PRIVATE, LocationKeyword.COZINESS));
        setField(request, "availableMediaType", AvailableMediaType.ALL);
        setField(request, "availableTimes", 3);
        setField(request, "latitude", 37.5d);
        setField(request, "longitude", 127.0d);
        return request;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
