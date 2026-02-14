package project.luckybooky.domain.location.repository.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import project.luckybooky.domain.location.entity.Location;
import project.luckybooky.domain.location.entity.type.AvailableMediaType;
import project.luckybooky.domain.location.entity.type.LocationKeyword;
import project.luckybooky.domain.location.repository.LocationRepository;
import project.luckybooky.global.util.DummyDataInit;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@DummyDataInit
@Order(1)
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
//                    Location.builder()
//                            .locationName("시네마테크 KOFA 1관")
//                            .address("서울특별시 마포구 상암동 1602 DMC 단지")
//                            .pricePerHour(40000)
//                            .locationKeywordList(keywords(
//                                    LocationKeyword.LARGE_SCALE,
//                                    LocationKeyword.FOR_GROUP,
//                                    LocationKeyword.LIVE_FEEL,
//                                    LocationKeyword.MAJESTIC
//                            ))
//                            .seatCount(321)
//                            .hasDisabledSeat(Boolean.TRUE)
//                            .locationImageUrl("https://kr.object.ncloudstorage.com/movie-bookie-storage/KOFA.jpg")
//                            .availableMediaType(AvailableMediaType.ALL)
//                            .availableTimes(0)
//                            .isStartTimeRestricted(Boolean.FALSE)
//                            .latitude(37.577608)
//                            .longitude(126.889993)
//                            .build(),
//                    Location.builder()
//                            .locationName("시네마테크 KOFA 2관")
//                            .address("서울특별시 마포구 상암동 1602 DMC 단지")
//                            .pricePerHour(20000)
//                            .locationKeywordList(keywords(
//                                    LocationKeyword.MEDIUM_SCALE,
//                                    LocationKeyword.NORMAL,
//                                    LocationKeyword.IMMERSION
//                            ))
//                            .seatCount(150)
//                            .hasDisabledSeat(Boolean.TRUE)
//                            .locationImageUrl("https://kr.object.ncloudstorage.com/movie-bookie-storage/KOFA.jpg")
//                            .availableMediaType(AvailableMediaType.ALL)
//                            .availableTimes(0)
//                            .isStartTimeRestricted(Boolean.FALSE)
//                            .latitude(37.577608)
//                            .longitude(126.889993)
//                            .build(),
                    Location.builder()
                            .locationName("코엑스 프라이빗 1호")
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
                            .locationImageUrl("https://kr.object.ncloudstorage.com/movie-bookie-storage/%EC%BD%94%EC%97%91%EC%8A%A4%201%ED%98%B8.png")
                            .availableMediaType(AvailableMediaType.ALL)
                            .availableTimes(3)
                            .isStartTimeRestricted(Boolean.FALSE)
                            .latitude(37.511483)
                            .longitude(127.060337)
                            .build(),
                    Location.builder()
                            .locationName("코엑스 프라이빗 2호")
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
                            .locationImageUrl("https://kr.object.ncloudstorage.com/movie-bookie-storage/%EC%BD%94%EC%97%91%EC%8A%A4_2%ED%98%B8.png")
                            .availableMediaType(AvailableMediaType.ALL)
                            .availableTimes(3)
                            .isStartTimeRestricted(Boolean.FALSE)
                            .latitude(37.511483)
                            .longitude(127.060337)
                            .build(),
//                    Location.builder()
//                            .locationName("필름포럼 1관")
//                            .address("서울특별시 서대문구 대신동 85")
//                            .pricePerHour(300000)
//                            .locationKeywordList(keywords(
//                                    LocationKeyword.MEDIUM_SCALE,
//                                    LocationKeyword.NORMAL,
//                                    LocationKeyword.IMMERSION
//                            ))
//                            .seatCount(90)
//                            .hasDisabledSeat(Boolean.FALSE)
//                            .locationImageUrl("https://kr.object.ncloudstorage.com/movie-bookie-storage/%ED%95%84%EB%A6%84%ED%8F%AC%EB%9F%BC%201%EA%B4%80.png")
//                            .availableMediaType(AvailableMediaType.ALL)
//                            .availableTimes(0)
//                            .isStartTimeRestricted(Boolean.FALSE)
//                            .latitude(37.567306)
//                            .longitude(126.943295)
//                            .build(),
//                    Location.builder()
//                            .locationName("필름포럼 2관")
//                            .address("서울특별시 서대문구 대신동 85")
//                            .pricePerHour(300000)
//                            .locationKeywordList(keywords(
//                                    LocationKeyword.MEDIUM_SCALE,
//                                    LocationKeyword.NORMAL,
//                                    LocationKeyword.IMMERSION
//                            ))
//                            .seatCount(52)
//                            .hasDisabledSeat(Boolean.FALSE)
//                            .locationImageUrl("https://kr.object.ncloudstorage.com/movie-bookie-storage/%ED%95%84%EB%A6%84%ED%8F%AC%EB%9F%BC_2%EA%B4%80.png")
//                            .availableMediaType(AvailableMediaType.ALL)
//                            .availableTimes(0)
//                            .isStartTimeRestricted(Boolean.FALSE)
//                            .latitude(37.567306)
//                            .longitude(126.943295)
//                            .build(),
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
                            .locationImageUrl("https://kr.object.ncloudstorage.com/movie-bookie-storage/%EC%9A%A9%EC%82%B0%20%EC%B9%A0%EC%9D%B8%EB%8D%94%EC%8B%9C%EB%84%A4%EB%A7%88.png")
                            .availableMediaType(AvailableMediaType.ALL)
                            .availableTimes(4)
                            .isStartTimeRestricted(Boolean.FALSE)
                            .latitude(37.535444)
                            .longitude(126.993316)
                            .build(),
                    Location.builder()
                            .locationName("상상스위트")
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
                            .locationImageUrl("https://kr.object.ncloudstorage.com/movie-bookie-storage/%EC%83%81%EC%83%81%EB%A7%88%EB%8B%B9.png")
                            .availableMediaType(AvailableMediaType.ALL)
                            .availableTimes(0)
                            .isStartTimeRestricted(Boolean.FALSE)
                            .latitude(37.551010427488)
                            .longitude(126.9211403551)
                            .build(),
                    Location.builder()
                            .locationName("을지영화관")
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
                            .locationImageUrl("https://kr.object.ncloudstorage.com/movie-bookie-storage/%EC%9D%84%EC%A7%80%EC%98%81%ED%99%94%EA%B4%80.png")
                            .availableMediaType(AvailableMediaType.ALL)
                            .availableTimes(3)
                            .isStartTimeRestricted(Boolean.FALSE)
                            .latitude(37.565864)
                            .longitude(126.992908)
                            .build(),
                    Location.builder()
                            .locationName("시글루 건대 ROOM1")
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
                            .locationImageUrl("https://kr.object.ncloudstorage.com/movie-bookie-storage/%EC%8B%9C%EA%B8%80%EB%A3%A8%20R1.png")
                            .availableMediaType(AvailableMediaType.ALL)
                            .availableTimes(3)
                            .isStartTimeRestricted(Boolean.TRUE)
                            .allowedStartTimes(allowedStartTimes(
                                    "12:00", "09:30", "13:00", "16:30", "20:00"
                            ))
                            .latitude(37.541855)
                            .longitude(127.066434)
                            .build(),
                    Location.builder()
                            .locationName("시글루 건대 ROOM2")
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
                            .locationImageUrl("https://kr.object.ncloudstorage.com/movie-bookie-storage/%EC%8B%9C%EA%B8%80%EB%A3%A8_%EA%B1%B4%EB%8C%80%EC%A0%90_%EB%A3%B82.png")
                            .availableMediaType(AvailableMediaType.ALL)
                            .availableTimes(3)
                            .isStartTimeRestricted(Boolean.TRUE)
                            .allowedStartTimes(allowedStartTimes(
                                    "12:00", "10:00", "13:30", "17:00", "20:30"
                            ))
                            .latitude(37.541855)
                            .longitude(127.066434)
                            .build(),
                    Location.builder()
                            .locationName("라이크어시네마 석촌")
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
                            .locationImageUrl("https://kr.object.ncloudstorage.com/movie-bookie-storage/%EB%9D%BC%EC%9D%B4%ED%81%AC%EC%96%B4%EC%8B%9C%EB%84%A4%EB%A7%88.png")
                            .availableMediaType(AvailableMediaType.ALL)
                            .availableTimes(2)
                            .isStartTimeRestricted(Boolean.FALSE)
                            .latitude(37.502612)
                            .longitude(127.110684)
                            .build(),
                    Location.builder()
                            .locationName("골드클래스 영등포")
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
                            .locationImageUrl("https://kr.object.ncloudstorage.com/movie-bookie-storage/CGV%20%EC%98%81%EB%93%B1%ED%8F%AC.png")
                            .availableMediaType(AvailableMediaType.ONLY_OTHERS)
                            .availableTimes(0)
                            .isStartTimeRestricted(Boolean.FALSE)
                            .latitude(37.517993)
                            .longitude(126.904780)
                            .build(),
                    Location.builder()
                            .locationName("골드클래스 용산")
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
                            .locationImageUrl("https://kr.object.ncloudstorage.com/movie-bookie-storage/%EC%98%81%ED%99%94%EA%B4%80_%EA%B3%A8%EB%93%9C_%ED%81%B4%EB%9E%98%EC%8A%A4_%EC%9A%A9%EC%82%B0.jpg")
                            .availableMediaType(AvailableMediaType.ONLY_OTHERS)
                            .availableTimes(0)
                            .isStartTimeRestricted(Boolean.FALSE)
                            .latitude(37.529129)
                            .longitude(126.965489)
                            .build(),
                    Location.builder()
                            .locationName("골드클래스 왕십리")
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
                            .locationImageUrl("https://kr.object.ncloudstorage.com/movie-bookie-storage/CGV%20%EC%99%95%EC%8B%AD%EB%A6%AC.png")
                            .availableMediaType(AvailableMediaType.ONLY_OTHERS)
                            .availableTimes(0)
                            .isStartTimeRestricted(Boolean.FALSE)
                            .latitude(37.561470)
                            .longitude(127.037277)
                            .build(),
                    Location.builder()
                            .locationName("시글루 잠실 ROOM1")
                            .address("서울 송파구 석촌호수로 284 지하1층 103호")
                            .pricePerHour(25000)
                            .locationKeywordList(keywords(
                                    LocationKeyword.IMMERSION,
                                    LocationKeyword.PRIVATE,
                                    LocationKeyword.COZINESS
                            ))
                            .seatCount(10)
                            .hasDisabledSeat(Boolean.FALSE)
                            .locationImageUrl("https://kr.object.ncloudstorage.com/movie-bookie-storage/%EC%8B%9C%EA%B8%80%EB%A3%A8%20%EC%9E%A0%EC%8B%A4%EC%A0%90%20%28ROOM1%29.png")
                            .availableMediaType(AvailableMediaType.ALL)
                            .availableTimes(3)
                            .isStartTimeRestricted(Boolean.FALSE)
                            .latitude(37.51079)
                            .longitude(127.106884)
                            .build(),
                    Location.builder()
                            .locationName("시글루 잠실 ROOM2")
                            .address("서울 송파구 석촌호수로 284 지하1층 103호")
                            .pricePerHour(25000)
                            .locationKeywordList(keywords(
                                    LocationKeyword.IMMERSION,
                                    LocationKeyword.PRIVATE,
                                    LocationKeyword.COZINESS
                            ))
                            .seatCount(10)
                            .hasDisabledSeat(Boolean.FALSE)
                            .locationImageUrl("https://kr.object.ncloudstorage.com/movie-bookie-storage/%EC%8B%9C%EA%B8%80%EB%A3%A8%20%EC%9E%A0%EC%8B%A4%EC%A0%90%20%28ROOM2%29.png")
                            .availableMediaType(AvailableMediaType.ALL)
                            .availableTimes(3)
                            .isStartTimeRestricted(Boolean.FALSE)
                            .latitude(37.51079)
                            .longitude(127.106884)
                            .build(),
                    Location.builder()
                            .locationName("서울살롱 신촌 1호점")
                            .address(" 서울 마포구 신촌로 20길 18")
                            .pricePerHour(60000)
                            .locationKeywordList(keywords(
                                    LocationKeyword.NORMAL,
                                    LocationKeyword.IMMERSION
                            ))
                            .seatCount(60)
                            .hasDisabledSeat(Boolean.FALSE)
                            .locationImageUrl("https://kr.object.ncloudstorage.com/movie-bookie-storage/%EC%84%9C%EC%9A%B8%EC%82%B4%EB%A1%B1%20%EC%8B%A0%EC%B4%8C%201%ED%98%B8%EC%A0%90.png")
                            .availableMediaType(AvailableMediaType.ALL)
                            .availableTimes(0)
                            .isStartTimeRestricted(Boolean.FALSE)
                            .latitude(37.554492)
                            .longitude(126.938479)
                            .build()
            );

            locationRepository.saveAll(locationList);
        }
    }
}
