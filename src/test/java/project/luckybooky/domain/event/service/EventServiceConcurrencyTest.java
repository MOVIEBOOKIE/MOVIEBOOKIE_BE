package project.luckybooky.domain.event.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import project.luckybooky.domain.category.service.CategoryService;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.event.repository.EventRepository;
import project.luckybooky.domain.location.service.LocationService;
import project.luckybooky.domain.participation.repository.ParticipationRepository;
import project.luckybooky.domain.ticket.service.TicketService;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.entity.UserType;
import project.luckybooky.domain.user.repository.UserRepository;
import project.luckybooky.domain.user.service.UserTypeService;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;
import project.luckybooky.global.repository.LockRepository;
import project.luckybooky.global.service.NCPStorageService;

@DataJpaTest
@EnableJpaAuditing
@Import({LockRepository.class, EventServiceConcurrencyTest.TestConfig.class})
class EventServiceConcurrencyTest {

  @Autowired
  private EventRepository eventRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ParticipationRepository participationRepository;

  @Autowired
  private EventService eventService;

  @TestConfiguration
  static class TestConfig {

    @Bean
    UserTypeService userTypeService() {
      return Mockito.mock(UserTypeService.class);
    }

    @Bean
    NCPStorageService ncpStorageService() {
      return Mockito.mock(NCPStorageService.class);
    }

    @Bean
    LocationService locationService() {
      return Mockito.mock(LocationService.class);
    }

    @Bean
    CategoryService categoryService() {
      return Mockito.mock(CategoryService.class);
    }

    @Bean
    TicketService ticketService() {
      return Mockito.mock(TicketService.class);
    }

    @Bean
    ApplicationEventPublisher applicationEventPublisher() {
      return Mockito.mock(ApplicationEventPublisher.class);
    }

    @Bean
    EventService eventService(
        EventRepository eventRepository,
        UserTypeService userTypeService,
        ParticipationRepository participationRepository,
        NCPStorageService ncpStorageService,
        LocationService locationService,
        CategoryService categoryService,
        TicketService ticketService,
        ApplicationEventPublisher publisher,
        UserRepository userRepository,
        LockRepository lockRepository
    ) {
      return new EventService(
          eventRepository,
          userTypeService,
          participationRepository,
          ncpStorageService,
          locationService,
          categoryService,
          ticketService,
          publisher,
          userRepository,
          lockRepository
      );
    }
  }

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  @DisplayName("실제 DB 환경에서 100명 동시 신청 시 최대 인원을 초과하지 않는다")
  void concurrentRegisterEvent_withRealDb() throws InterruptedException {
    int maxParticipants = 10;
    int threadCount = 100;

    List<Long> userIds = new ArrayList<>();
    for (int i = 0; i < threadCount; i++) {
      User user = User.builder()
          .email("user" + i + "@test.com")
          .username("user" + i)
          .userType(UserType.MOVIE_DETAIL_COLLECTOR)
          .build();
      userRepository.saveAndFlush(user);
      userIds.add(user.getId());
    }

    Event event = Event.builder()
        .mediaTitle("테스트 미디어")
        .eventTitle("테스트 이벤트")
        .description("동시성 테스트 이벤트")
        .eventDate(LocalDate.of(2099, 12, 31))
        .eventStartTime("10:00")
        .eventEndTime("12:00")
        .recruitmentStart(LocalDate.now().minusDays(1))
        .recruitmentEnd(LocalDate.now().plusDays(30))
        .estimatedPrice(10000)
        .posterImageUrl("http://example.com/poster")
        .minParticipants(1)
        .maxParticipants(maxParticipants)
        .currentParticipants(0)
        .isPublic(true)
        .build();
    eventRepository.saveAndFlush(event);
    Long eventId = event.getId();

    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(threadCount);

    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failCount = new AtomicInteger(0);

    for (int i = 0; i < threadCount; i++) {
      final Long userId = userIds.get(i);
      executorService.submit(() -> {
        try {
          startLatch.await();
          try {
            eventService.registerEvent(userId, eventId);
            successCount.incrementAndGet();
          } catch (BusinessException ex) {
            failCount.incrementAndGet();
          } catch (Exception ex) {
            failCount.incrementAndGet();
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } finally {
          doneLatch.countDown();
        }
      });
    }

    long startTime = System.currentTimeMillis();
    startLatch.countDown();
    boolean finished = doneLatch.await(30, TimeUnit.SECONDS);
    executorService.shutdownNow();
    long elapsed = System.currentTimeMillis() - startTime;

    assertThat(finished).isTrue();

    Event reloadedEvent = eventRepository.findById(eventId).orElseThrow();
    int currentParticipants = reloadedEvent.getCurrentParticipants();
    long participationRows = participationRepository.countByEventId(eventId);

    System.out.println("확인을 위해 최대 이벤트 신청 가능 인원: " + maxParticipants);
    System.out.println("동시 요청(쓰레드) 수: " + threadCount);
    System.out.println("이벤트 신청 성공 쓰레드 수: " + successCount.get());
    System.out.println("이벤트 신청 실패 쓰레드 수: " + failCount.get());
    System.out.println("최종 참여 인원(currentParticipants): " + currentParticipants);
    System.out.println("Participation 테이블 row 수: " + participationRows);
    System.out.println("총 소요 시간(ms): " + elapsed);

    assertThat(successCount.get()).isEqualTo(maxParticipants);
    assertThat(failCount.get()).isEqualTo(threadCount - maxParticipants);
    assertThat(currentParticipants).isEqualTo(maxParticipants);
    assertThat(participationRows).isEqualTo(maxParticipants);
  }
}
