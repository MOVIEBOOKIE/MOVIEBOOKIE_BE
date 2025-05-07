package project.luckybooky.domain.location.repository.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import project.luckybooky.domain.location.entity.Location;
import project.luckybooky.domain.location.entity.type.LocationKeyword;
import project.luckybooky.domain.location.repository.LocationRepository;
import project.luckybooky.global.util.DummyDataInit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@DummyDataInit
public class LocationInitializer implements ApplicationRunner {
    private final LocationRepository locationRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (locationRepository.count() > 0) {
            log.info("[Location] 더미 데이터 존재");
        } else {
            List<Location> locationList = new ArrayList<>();

            HashSet<LocationKeyword> keywords1 = new HashSet<>();
            keywords1.add(LocationKeyword.LARGE_SCALE);
            keywords1.add(LocationKeyword.FOR_GROUP);
            keywords1.add(LocationKeyword.LIVE_FEEL);
            keywords1.add(LocationKeyword.MAJESTIC);
            Location location1 = Location.builder()
                    .locationName("한국영상자료원 시네마파크 KOFA 1관")
                    .address("서울특별시 마포구 상암동 1602 DMC 단지")
                    .pricePerHour(40000L)
                    .locationKeywordList(keywords1)
                    .hasDisabledSeat(Boolean.TRUE)
                    .build();

            HashSet<LocationKeyword> keywords2 = new HashSet<>();
            keywords2.add(LocationKeyword.SMALL_SCALE);
            keywords2.add(LocationKeyword.PREMIUM);
            keywords2.add(LocationKeyword.FOR_COUPLE);
            keywords2.add(LocationKeyword.FOR_FAMILY);
            Location location2 = Location.builder()
                    .locationName("코엑스 더 부티크 프라이빗 1관")
                    .address("서울 마포구 상암동 1602 DMC 단지")
                    .pricePerHour(20000L)
                    .locationKeywordList(keywords2)
                    .hasDisabledSeat(Boolean.TRUE)
                    .build();

            locationList.add(location1);
            locationList.add(location2);

            locationRepository.saveAll(locationList);
        }
    }
}
