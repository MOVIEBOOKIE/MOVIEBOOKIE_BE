package project.luckybooky.domain.location.repository.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import project.luckybooky.domain.location.entity.Location;
import project.luckybooky.domain.location.repository.LocationRepository;
import project.luckybooky.global.util.DummyDataInit;

import java.util.ArrayList;
import java.util.List;

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

            Location 롯데시네마_신촌관 = Location.builder()
                    .locationName("롯데시네마 신촌관")
                    .address("서울특별시 마포구 양화로 176")
                    .build();

            Location 롯데시네마_안양일번가관 = Location.builder()
                    .locationName("롯데시네마 안양일번가관")
                    .address("경기도 안양시 만안구 안양로 311")
                    .build();

            Location 롯데시네마_범계관 = Location.builder()
                    .locationName("롯데시네마 범계관")
                    .address("경기도 안양시 동안구 시민대로 180")
                    .build();

            locationList.add(롯데시네마_신촌관);
            locationList.add(롯데시네마_안양일번가관);
            locationList.add(롯데시네마_범계관);

            locationRepository.saveAll(locationList);
        }
    }
}
