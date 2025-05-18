package project.luckybooky.domain.event.repository.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.DependsOn;
import project.luckybooky.domain.category.entity.Category;
import project.luckybooky.domain.category.repository.CategoryRepository;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.event.repository.EventRepository;
import project.luckybooky.domain.location.entity.Location;
import project.luckybooky.domain.location.repository.LocationRepository;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;
import project.luckybooky.global.util.DummyDataInitForDev;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@DummyDataInitForDev
@RequiredArgsConstructor
@DependsOn({"userInitializer", "locationInitializer", "categoryInitializer"})
public class EventInitializer implements ApplicationRunner {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (eventRepository.count() > 0) {
            log.info("[Location] 데이터 존재");
        } else {
            Category category = categoryRepository.findById(1L).orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
            Location location = locationRepository.findById(1L).orElseThrow(() -> new BusinessException(ErrorCode.LOCATION_NOT_FOUND));

            List<Event> eventList = new ArrayList<>();

            Event event1 = Event.builder()
                    .category(category)
                    .eventDate(LocalDate.now().plusMonths(3L))
                    .eventStartTime("14:00")
                    .eventEndTime("17:00")
                    .mediaTitle("더 폴: 오디어스와 환상의 문")
                    .eventTitle("더 폴은 진짜진짜 명작입니다!")
                    .description("이번에 영화 단체 관람 같이 볼 사람 구해요!!")
                    .estimatedPrice(30000)
                    .location(location)
                    .recruitmentStart(LocalDate.now().plusMonths(1L))
                    .recruitmentEnd(LocalDate.now().plusMonths(2L))
                    .minParticipants(1)
                    .maxParticipants(100)
                    .posterImageUrl("https://kr.object.ncloudstorage.com/movie-bookie-storage/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA%202025-05-14%20%E1%84%8B%E1%85%A9%E1%84%8C%E1%85%A5%E1%86%AB%2011.06.05.png")
                    .build();

            Event event2 = Event.builder()
                    .category(category)
                    .eventDate(LocalDate.now().plusMonths(3L))
                    .eventStartTime("14:00")
                    .eventEndTime("17:00")
                    .mediaTitle("극한직업")
                    .eventTitle("극한직업은 진짜진짜 명작입니다!")
                    .description("이번에 영화 단체 관람 같이 볼 사람 구해요!!")
                    .estimatedPrice(30000)
                    .location(location)
                    .recruitmentStart(LocalDate.now().plusMonths(1L))
                    .recruitmentEnd(LocalDate.now().plusMonths(2L))
                    .minParticipants(1)
                    .maxParticipants(100)
                    .posterImageUrl("https://kr.object.ncloudstorage.com/movie-bookie-storage/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA%202025-05-14%20%E1%84%8B%E1%85%A9%E1%84%8C%E1%85%A5%E1%86%AB%2011.06.05.png")
                    .build();

            eventList.add(event1);
            eventList.add(event2);

            eventRepository.saveAll(eventList);
        }
    }
}
