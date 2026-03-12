package project.luckybooky.domain.event.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import project.luckybooky.domain.category.service.CategoryService;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.event.repository.EventRepository;
import project.luckybooky.domain.location.service.LocationService;
import project.luckybooky.domain.notification.event.app.ParticipantNotificationEvent;
import project.luckybooky.domain.participation.entity.Participation;
import project.luckybooky.domain.participation.repository.ParticipationRepository;
import project.luckybooky.domain.ticket.service.TicketService;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.repository.UserRepository;
import project.luckybooky.domain.user.service.UserTypeService;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;
import project.luckybooky.global.repository.LockRepository;
import project.luckybooky.global.service.NCPStorageService;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

  @Mock
  private EventRepository eventRepository;
  @Mock
  private UserTypeService userTypeService;
  @Mock
  private ParticipationRepository participationRepository;
  @Mock
  private NCPStorageService s3Service;
  @Mock
  private LocationService locationService;
  @Mock
  private CategoryService categoryService;
  @Mock
  private TicketService ticketService;
  @Mock
  private ApplicationEventPublisher publisher;
  @Mock
  private UserRepository userRepository;
  @Mock
  private LockRepository lockRepository;

  private EventService eventService;

  @BeforeEach
  void setUp() {
    eventService = new EventService(
        eventRepository,
        userTypeService,
        participationRepository,
        s3Service,
        locationService,
        categoryService,
        ticketService,
        publisher,
        userRepository,
        lockRepository
    );
  }

  private Event createEvent(Long eventId, int maxParticipants, int currentParticipants,
      LocalDate eventDate) {
    return Event.builder()
        .id(eventId)
        .mediaTitle("미디어 제목")
        .eventTitle("이벤트 제목")
        .description("설명")
        .eventDate(eventDate)
        .eventStartTime("10:00")
        .eventEndTime("12:00")
        .recruitmentStart(eventDate.minusDays(1))
        .recruitmentEnd(eventDate.plusDays(1))
        .estimatedPrice(10000)
        .posterImageUrl("http://example.com/poster")
        .minParticipants(1)
        .maxParticipants(maxParticipants)
        .currentParticipants(currentParticipants)
        .isPublic(true)
        .build();
  }

  private User createUser(Long userId) {
    return User.builder()
        .id(userId)
        .email("test" + userId + "@example.com")
        .username("tester" + userId)
        .build();
  }

  @Test
  @DisplayName("이벤트 신청 성공 - 정원 미달 & 해당 날짜에 다른 이벤트 미참여")
  void registerEvent_success() {
    Long userId = 1L;
    Long eventId = 10L;
    LocalDate eventDate = LocalDate.now();

    User user = createUser(userId);
    Event event = createEvent(eventId, 5, 0, eventDate);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
    when(participationRepository.existsByUserIdAndEventId(userId, eventId)).thenReturn(false);
    when(participationRepository.existsByUserIdAndEventDate(userId, eventDate)).thenReturn(false);
    when(participationRepository.save(any(Participation.class))).thenAnswer(invocation -> invocation.getArgument(0));

    eventService.registerEvent(userId, eventId);

    assertThat(event.getCurrentParticipants()).isEqualTo(1);
    verify(participationRepository).save(any(Participation.class));
    verify(publisher).publishEvent(any(ParticipantNotificationEvent.class));
  }

  @Test
  @DisplayName("이벤트 신청 실패 - 정원이 이미 가득 찬 경우(EVENT_FULL)")
  void registerEvent_fail_whenEventFull() {
    Long userId = 1L;
    Long eventId = 10L;
    LocalDate eventDate = LocalDate.now();

    User user = createUser(userId);
    int maxParticipants = 3;
    Event event = createEvent(eventId, maxParticipants, maxParticipants, eventDate);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
    when(participationRepository.existsByUserIdAndEventId(userId, eventId)).thenReturn(false);
    when(participationRepository.existsByUserIdAndEventDate(userId, eventDate)).thenReturn(false);

    assertThatThrownBy(() -> eventService.registerEvent(userId, eventId))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.EVENT_FULL);

    verifyNoMoreInteractions(participationRepository);
  }

  @Test
  @DisplayName("이벤트 신청 실패 - 이미 해당 이벤트에 신청한 경우(ALREADY_REGISTERED_EVENT)")
  void registerEvent_fail_whenAlreadyRegistered() {
    Long userId = 1L;
    Long eventId = 10L;
    LocalDate eventDate = LocalDate.now();

    User user = createUser(userId);
    Event event = createEvent(eventId, 20, 0, eventDate);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
    when(participationRepository.existsByUserIdAndEventId(userId, eventId)).thenReturn(true);

    assertThatThrownBy(() -> eventService.registerEvent(userId, eventId))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.ALREADY_REGISTERED_EVENT);
  }

  @Test
  @DisplayName("여러 번 이벤트 신청을 시도해도 최대 인원까지만 성공하고 그 이후는 실패해야 한다")
  void registerEvent_multipleAttempts_respectsMaxParticipants() {
    Long eventId = 10L;
    LocalDate eventDate = LocalDate.now();

    int maxParticipants = 5;
    Event event = createEvent(eventId, maxParticipants, 0, eventDate);

    when(userRepository.findById(anyLong()))
        .thenAnswer(invocation -> Optional.of(createUser(invocation.getArgument(0, Long.class))));
    when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
    when(participationRepository.existsByUserIdAndEventId(anyLong(), eq(eventId))).thenReturn(false);
    when(participationRepository.existsByUserIdAndEventDate(anyLong(), eq(eventDate))).thenReturn(false);

    int attempts = 15;
    int successCount = 0;
    int failCount = 0;

    for (long userId = 1L; userId <= attempts; userId++) {
      try {
        eventService.registerEvent(userId, eventId);
        successCount++;
      } catch (BusinessException ex) {
        failCount++;
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.EVENT_FULL);
      }
    }

    assertThat(successCount).isEqualTo(maxParticipants);
    assertThat(failCount).isEqualTo(attempts - maxParticipants);
    assertThat(event.getCurrentParticipants()).isEqualTo(maxParticipants);
  }

  @Test
  @DisplayName("다수의 쓰레드가 동시에 신청하더라도 최대 인원까지만 성공해야 한다")
  void registerEvent_concurrentRequests_respectsMaxParticipants() throws InterruptedException {
    Long eventId = 20L;
    LocalDate eventDate = LocalDate.now();

    int maxParticipants = 10;
    int threadCount = 200;

    Event event = createEvent(eventId, maxParticipants, 0, eventDate);

    when(userRepository.findById(anyLong()))
        .thenAnswer(invocation -> Optional.of(createUser(invocation.getArgument(0, Long.class))));
    when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
    when(participationRepository.existsByUserIdAndEventId(anyLong(), eq(eventId))).thenReturn(false);
    when(participationRepository.existsByUserIdAndEventDate(anyLong(), eq(eventDate))).thenReturn(false);

    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(threadCount);

    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failCount = new AtomicInteger(0);

    for (long userId = 1; userId <= threadCount; userId++) {
      final long uid = userId;
      executorService.submit(() -> {
        try {
          startLatch.await();
          try {
            eventService.registerEvent(uid, eventId);
            successCount.incrementAndGet();
          } catch (BusinessException ex) {
            failCount.incrementAndGet();
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.EVENT_FULL);
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } finally {
          doneLatch.countDown();
        }
      });
    }

    startLatch.countDown();
    boolean finished = doneLatch.await(10, TimeUnit.SECONDS);
    executorService.shutdownNow();

    assertThat(finished).isTrue();

    int success = successCount.get();
    int fail = failCount.get();

    assertThat(success + fail).isEqualTo(threadCount);
    assertThat(success).isEqualTo(maxParticipants);
    assertThat(fail).isEqualTo(threadCount - maxParticipants);
    assertThat(event.getCurrentParticipants()).isEqualTo(maxParticipants);
  }
}
