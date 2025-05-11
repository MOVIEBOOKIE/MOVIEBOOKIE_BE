package project.luckybooky.domain.location.repository.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import project.luckybooky.domain.location.entity.Location;
import project.luckybooky.domain.location.entity.type.AvailableMediaType;
import project.luckybooky.domain.location.entity.type.LocationKeyword;
import project.luckybooky.domain.location.repository.LocationRepository;
import project.luckybooky.global.util.DummyDataInit;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@DummyDataInit
public class LocationInitializer implements ApplicationRunner {
    private final LocationRepository locationRepository;

    // keyword set 생성 메서드
    private Set<LocationKeyword> keywords(LocationKeyword... keywords) {
        return new HashSet<>(Arrays.asList(keywords));
    }

    // 가능한 시작시간대 set 생성 메서드
    private Set<String> allowedStartTimes(String... times) {
        return new HashSet<>(Arrays.asList(times));
    }

    @Override
    public void run(ApplicationArguments args) {
        if (locationRepository.count() > 0) {
            log.info("[Location] 데이터 존재");
        } else {
            List<Location> locationList = Arrays.asList(
                    Location.builder()
                            .locationName("한국영상자료원 시네마테크KOFA 1관")
                            .address("서울특별시 마포구 상암동 1602 DMC 단지")
                            .pricePerHour(40000)
                            .locationKeywordList(keywords(
                                    LocationKeyword.LARGE_SCALE,
                                    LocationKeyword.FOR_GROUP,
                                    LocationKeyword.LIVE_FEEL,
                                    LocationKeyword.MAJESTIC
                            ))
                            .seatCount(321)
                            .hasDisabledSeat(Boolean.TRUE)
                            .locationImageUrl("https://movie-bookie-upload-files.s3.ap-northeast-2.amazonaws.com/KOFA.png")
                            .availableMediaType(AvailableMediaType.ALL)
                            .availableTimes(0)
                            .isStartTimeRestricted(Boolean.FALSE)
                            .build(),
                    Location.builder()
                            .locationName("한국영상자료원 시네마테크KOFA 2관")
                            .address("서울특별시 마포구 상암동 1602 DMC 단지")
                            .pricePerHour(20000)
                            .locationKeywordList(keywords(
                                    LocationKeyword.MEDIUM_SCALE,
                                    LocationKeyword.NORMAL,
                                    LocationKeyword.IMMERSION
                            ))
                            .seatCount(150)
                            .hasDisabledSeat(Boolean.TRUE)
                            .locationImageUrl("https://movie-bookie-upload-files.s3.ap-northeast-2.amazonaws.com/KOFA.png")
                            .availableMediaType(AvailableMediaType.ALL)
                            .availableTimes(0)
                            .isStartTimeRestricted(Boolean.FALSE)
                            .build(),
                    Location.builder()
                            .locationName("코엑스 더 부티크 프라이빗 1관")
                            .address("서울 강남구 삼성동 159")
                            .pricePerHour(200000)
                            .locationKeywordList(keywords(
                                    LocationKeyword.SMALL_SCALE,
                                    LocationKeyword.PREMIUM,
                                    LocationKeyword.PREMIUM,
                                    LocationKeyword.COZINESS
                            ))
                            .seatCount(8)
                            .hasDisabledSeat(Boolean.FALSE)
                            .locationImageUrl("https://movie-bookie-upload-files.s3.ap-northeast-2.amazonaws.com/%E1%84%8F%E1%85%A9%E1%84%8B%E1%85%A6%E1%86%A8%E1%84%89%E1%85%B3+%E1%84%83%E1%85%A5+%E1%84%87%E1%85%AE%E1%84%90%E1%85%B5%E1%84%8F%E1%85%B3+%E1%84%91%E1%85%B3%E1%84%85%E1%85%A1%E1%84%8B%E1%85%B5%E1%84%87%E1%85%B5%E1%86%BA+1%E1%84%92%E1%85%A9.png")
                            .availableMediaType(AvailableMediaType.ALL)
                            .availableTimes(3)
                            .isStartTimeRestricted(Boolean.FALSE)
                            .build(),
                    Location.builder()
                            .locationName("코엑스 더 부티크 프라이빗 2관")
                            .address("서울 강남구 삼성동 159")
                            .pricePerHour(200000)
                            .locationKeywordList(keywords(
                                    LocationKeyword.SMALL_SCALE,
                                    LocationKeyword.PREMIUM,
                                    LocationKeyword.PRIVATE,
                                    LocationKeyword.COZINESS
                            ))
                            .seatCount(10)
                            .hasDisabledSeat(Boolean.FALSE)
                            .locationImageUrl("https://movie-bookie-upload-files.s3.ap-northeast-2.amazonaws.com/%E1%84%8F%E1%85%A9%E1%84%8B%E1%85%A6%E1%86%A8%E1%84%89%E1%85%B3+%E1%84%83%E1%85%A5+%E1%84%87%E1%85%AE%E1%84%90%E1%85%B5%E1%84%8F%E1%85%B3+%E1%84%91%E1%85%B3%E1%84%85%E1%85%A1%E1%84%8B%E1%85%B5%E1%84%87%E1%85%B5%E1%86%BA+2%E1%84%92%E1%85%A9.png")
                            .availableMediaType(AvailableMediaType.ALL)
                            .availableTimes(3)
                            .isStartTimeRestricted(Boolean.FALSE)
                            .build(),
                    Location.builder()
                            .locationName("좋은 영화관 - 필름 포럼 1관")
                            .address("서울특별시 서대문구 대신동 85")
                            .pricePerHour(300000)
                            .locationKeywordList(keywords(
                                    LocationKeyword.MEDIUM_SCALE,
                                    LocationKeyword.NORMAL,
                                    LocationKeyword.IMMERSION
                            ))
                            .seatCount(90)
                            .hasDisabledSeat(Boolean.FALSE)
                            .locationImageUrl("https://movie-bookie-upload-files.s3.ap-northeast-2.amazonaws.com/%E1%84%91%E1%85%B5%E1%86%AF%E1%84%85%E1%85%B3%E1%86%B7%E1%84%91%E1%85%A9%E1%84%85%E1%85%A5%E1%86%B7+1%E1%84%80%E1%85%AA%E1%86%AB.png")
                            .availableMediaType(AvailableMediaType.ALL)
                            .availableTimes(0)
                            .isStartTimeRestricted(Boolean.FALSE)
                            .build(),
                    Location.builder()
                            .locationName("좋은 영화관 - 필름 포럼 2관")
                            .address("서울특별시 서대문구 대신동 85")
                            .pricePerHour(300000)
                            .locationKeywordList(keywords(
                                    LocationKeyword.MEDIUM_SCALE,
                                    LocationKeyword.NORMAL,
                                    LocationKeyword.IMMERSION
                            ))
                            .seatCount(52)
                            .hasDisabledSeat(Boolean.FALSE)
                            .locationImageUrl("https://movie-bookie-upload-files.s3.ap-northeast-2.amazonaws.com/%E1%84%91%E1%85%B5%E1%86%AF%E1%84%85%E1%85%B3%E1%86%B7%E1%84%91%E1%85%A9%E1%84%85%E1%85%A5%E1%86%B7+2%E1%84%80%E1%85%AA%E1%86%AB.png")
                            .availableMediaType(AvailableMediaType.ALL)
                            .availableTimes(0)
                            .isStartTimeRestricted(Boolean.FALSE)
                            .build(),
                    Location.builder()
                            .locationName("용산 칠인더시네마")
                            .address("서울특별시 용산구 이태원동 305-5")
                            .pricePerHour(25000)
                            .locationKeywordList(keywords(
                                    LocationKeyword.SMALL_SCALE,
                                    LocationKeyword.PRIVATE,
                                    LocationKeyword.COZINESS,
                                    LocationKeyword.QUIETNESS
                            ))
                            .seatCount(12)
                            .hasDisabledSeat(Boolean.FALSE)
                            .locationImageUrl("https://movie-bookie-upload-files.s3.ap-northeast-2.amazonaws.com/%E1%84%8E%E1%85%B5%E1%86%AF%E1%84%8B%E1%85%B5%E1%86%AB%E1%84%83%E1%85%A5%E1%84%89%E1%85%B5%E1%84%82%E1%85%A6%E1%84%86%E1%85%A1.png")
                            .availableMediaType(AvailableMediaType.ALL)
                            .availableTimes(4)
                            .isStartTimeRestricted(Boolean.FALSE)
                            .build(),
                    Location.builder()
                            .locationName("상상 스위트")
                            .address("서울 마포구 서교동 367-5")
                            .pricePerHour(50000)
                            .locationKeywordList(keywords(
                                    LocationKeyword.SMALL_SCALE,
                                    LocationKeyword.PRIVATE,
                                    LocationKeyword.COZINESS,
                                    LocationKeyword.QUIETNESS
                            ))
                            .seatCount(15)
                            .hasDisabledSeat(Boolean.FALSE)
                            .locationImageUrl("https://movie-bookie-upload-files.s3.ap-northeast-2.amazonaws.com/%E1%84%89%E1%85%A1%E1%86%BC%E1%84%89%E1%85%A1%E1%86%BC%E1%84%89%E1%85%B3%E1%84%8B%E1%85%B1%E1%84%90%E1%85%B3.png")
                            .availableMediaType(AvailableMediaType.ALL)
                            .availableTimes(0)
                            .isStartTimeRestricted(Boolean.FALSE)
                            .build(),
                    Location.builder()
                            .locationName("을지 영화관")
                            .address("서울 중구 을지로3가 315-12")
                            .pricePerHour(32000)
                            .locationKeywordList(keywords(
                                    LocationKeyword.SMALL_SCALE,
                                    LocationKeyword.PRIVATE,
                                    LocationKeyword.COZINESS,
                                    LocationKeyword.QUIETNESS
                            ))
                            .seatCount(6)
                            .hasDisabledSeat(Boolean.FALSE)
                            .locationImageUrl("https://movie-bookie-upload-files.s3.ap-northeast-2.amazonaws.com/%E1%84%8B%E1%85%B3%E1%86%AF%E1%84%8C%E1%85%B5%E1%84%8B%E1%85%A7%E1%86%BC%E1%84%92%E1%85%AA%E1%84%80%E1%85%AA%E1%86%AB.png")
                            .availableMediaType(AvailableMediaType.ALL)
                            .availableTimes(3)
                            .isStartTimeRestricted(Boolean.FALSE)
                            .build(),
                    Location.builder()
                            .locationName("시글루 건대점 ROOM1")
                            .address("서울 광진구 화양동 49-13")
                            .pricePerHour(25000)
                            .locationKeywordList(keywords(
                                    LocationKeyword.SMALL_SCALE,
                                    LocationKeyword.PRIVATE,
                                    LocationKeyword.COZINESS,
                                    LocationKeyword.QUIETNESS
                            ))
                            .seatCount(8)
                            .hasDisabledSeat(Boolean.FALSE)
                            .locationImageUrl("https://movie-bookie-upload-files.s3.ap-northeast-2.amazonaws.com/%E1%84%89%E1%85%B5%E1%84%80%E1%85%B3%E1%86%AF%E1%84%85%E1%85%AE_%E1%84%80%E1%85%A5%E1%86%AB%E1%84%83%E1%85%A2%E1%84%8C%E1%85%A5%E1%86%B7_%E1%84%85%E1%85%AE%E1%86%B71.png")
                            .availableMediaType(AvailableMediaType.ALL)
                            .availableTimes(3)
                            .isStartTimeRestricted(Boolean.TRUE)
                            .allowedStartTimes(allowedStartTimes(
                                    "12:00", "09:30", "13:00", "16:30", "20:00"
                            ))
                            .build(),
                    Location.builder()
                            .locationName("시글루 건대점 ROOM2")
                            .address("서울 광진구 화양동 49-13")
                            .pricePerHour(25000)
                            .locationKeywordList(keywords(
                                    LocationKeyword.SMALL_SCALE,
                                    LocationKeyword.PRIVATE,
                                    LocationKeyword.COZINESS,
                                    LocationKeyword.QUIETNESS
                            ))
                            .seatCount(8)
                            .hasDisabledSeat(Boolean.FALSE)
                            .locationImageUrl("https://movie-bookie-upload-files.s3.ap-northeast-2.amazonaws.com/%E1%84%89%E1%85%B5%E1%84%80%E1%85%B3%E1%86%AF%E1%84%85%E1%85%AE_%E1%84%80%E1%85%A5%E1%86%AB%E1%84%83%E1%85%A2%E1%84%8C%E1%85%A5%E1%86%B7_%E1%84%85%E1%85%AE%E1%86%B72.png")
                            .availableMediaType(AvailableMediaType.ALL)
                            .availableTimes(3)
                            .isStartTimeRestricted(Boolean.TRUE)
                            .allowedStartTimes(allowedStartTimes(
                                    "12:00", "10:00", "13:30", "17:00", "20:30"
                            ))
                            .build(),
                    Location.builder()
                            .locationName("라이크어시네마 석촌점")
                            .address("서울 송파구 송파동 97-1")
                            .pricePerHour(15000)
                            .locationKeywordList(keywords(
                                    LocationKeyword.SMALL_SCALE,
                                    LocationKeyword.PRIVATE,
                                    LocationKeyword.COZINESS,
                                    LocationKeyword.QUIETNESS
                            ))
                            .seatCount(6)
                            .hasDisabledSeat(Boolean.FALSE)
                            .locationImageUrl("https://movie-bookie-upload-files.s3.ap-northeast-2.amazonaws.com/CGV+%E1%84%80%E1%85%A9%E1%86%AF%E1%84%83%E1%85%B3+%E1%84%8F%E1%85%B3%E1%86%AF%E1%84%85%E1%85%A2%E1%84%89%E1%85%B3+%E1%84%8B%E1%85%A7%E1%86%BC%E1%84%83%E1%85%B3%E1%86%BC%E1%84%91%E1%85%A9.png")
                            .availableMediaType(AvailableMediaType.ALL)
                            .availableTimes(2)
                            .isStartTimeRestricted(Boolean.FALSE)
                            .build(),
                    Location.builder()
                            .locationName("CGV 골드 클래스 영등포")
                            .address("서울 영등포구 영등포동4가 442")
                            .pricePerHour(40000)
                            .locationKeywordList(keywords(
                                    LocationKeyword.SMALL_SCALE,
                                    LocationKeyword.PREMIUM,
                                    LocationKeyword.COZINESS,
                                    LocationKeyword.QUIETNESS
                            ))
                            .seatCount(48)
                            .hasDisabledSeat(Boolean.FALSE)
                            .locationImageUrl("https://movie-bookie-upload-files.s3.ap-northeast-2.amazonaws.com/CGV+%E1%84%80%E1%85%A9%E1%86%AF%E1%84%83%E1%85%B3+%E1%84%8F%E1%85%B3%E1%86%AF%E1%84%85%E1%85%A2%E1%84%89%E1%85%B3+%E1%84%8B%E1%85%A7%E1%86%BC%E1%84%83%E1%85%B3%E1%86%BC%E1%84%91%E1%85%A9.png")
                            .availableMediaType(AvailableMediaType.ONLY_OTHERS)
                            .availableTimes(0)
                            .isStartTimeRestricted(Boolean.FALSE)
                            .build(),
                    Location.builder()
                            .locationName("CGV 골드 클래스 용산 아이파크몰")
                            .address("서울 용산구 한강로3가 40-999")
                            .pricePerHour(40000)
                            .locationKeywordList(keywords(
                                    LocationKeyword.SMALL_SCALE,
                                    LocationKeyword.PREMIUM,
                                    LocationKeyword.COZINESS,
                                    LocationKeyword.QUIETNESS
                            ))
                            .seatCount(38)
                            .hasDisabledSeat(Boolean.FALSE)
                            .locationImageUrl("https://movie-bookie-upload-files.s3.ap-northeast-2.amazonaws.com/CGV+%E1%84%80%E1%85%A9%E1%86%AF%E1%84%83%E1%85%B3+%E1%84%8F%E1%85%B3%E1%86%AF%E1%84%85%E1%85%A2%E1%84%89%E1%85%B3+%E1%84%8B%E1%85%AD%E1%86%BC%E1%84%89%E1%85%A1%E1%86%AB.jpg")
                            .availableMediaType(AvailableMediaType.ONLY_OTHERS)
                            .availableTimes(0)
                            .isStartTimeRestricted(Boolean.FALSE)
                            .build(),
                    Location.builder()
                            .locationName("CGV 골드 클래스 왕십리")
                            .address("서울 성동구 행당동 168-151")
                            .pricePerHour(40000)
                            .locationKeywordList(keywords(
                                    LocationKeyword.SMALL_SCALE,
                                    LocationKeyword.PREMIUM,
                                    LocationKeyword.COZINESS,
                                    LocationKeyword.QUIETNESS
                            ))
                            .seatCount(30)
                            .hasDisabledSeat(Boolean.FALSE)
                            .locationImageUrl("https://movie-bookie-upload-files.s3.ap-northeast-2.amazonaws.com/CGV+%E1%84%80%E1%85%A9%E1%86%AF%E1%84%83%E1%85%B3+%E1%84%8F%E1%85%B3%E1%86%AF%E1%84%85%E1%85%A2%E1%84%89%E1%85%B3+%E1%84%8B%E1%85%AA%E1%86%BC%E1%84%89%E1%85%B5%E1%86%B8%E1%84%85%E1%85%B5.jpeg")
                            .availableMediaType(AvailableMediaType.ONLY_OTHERS)
                            .availableTimes(0)
                            .isStartTimeRestricted(Boolean.FALSE)
                            .build()
            );

            locationRepository.saveAll(locationList);
        }
    }
}
