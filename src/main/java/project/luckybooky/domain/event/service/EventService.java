package project.luckybooky.domain.event.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import project.luckybooky.domain.admin.event.VenueRequestWebhookEvent;
import project.luckybooky.domain.category.entity.Category;
import project.luckybooky.domain.category.service.CategoryService;
import project.luckybooky.domain.event.converter.EventConverter;
import project.luckybooky.domain.event.dto.request.EventRequest;
import project.luckybooky.domain.event.dto.response.EventParticipantsResponse;
import project.luckybooky.domain.event.dto.response.EventResponse;
import project.luckybooky.domain.event.dto.response.ParticipantDTO;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.event.repository.EventRepository;
import project.luckybooky.domain.event.util.EventConstants;
import project.luckybooky.domain.location.entity.Location;
import project.luckybooky.domain.location.service.LocationService;
import project.luckybooky.domain.notification.event.HostNotificationEvent;
import project.luckybooky.domain.notification.event.ParticipantNotificationEvent;
import project.luckybooky.domain.notification.type.HostNotificationType;
import project.luckybooky.domain.notification.type.ParticipantNotificationType;
import project.luckybooky.domain.participation.converter.ParticipationConverter;
import project.luckybooky.domain.participation.entity.Participation;
import project.luckybooky.domain.participation.entity.type.ParticipateRole;
import project.luckybooky.domain.participation.repository.ParticipationRepository;
import project.luckybooky.domain.ticket.service.TicketService;
import project.luckybooky.domain.user.entity.ContentCategory;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.entity.UserType;
import project.luckybooky.domain.user.repository.UserRepository;
import project.luckybooky.domain.user.service.UserTypeService;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;
import project.luckybooky.global.service.NCPStorageService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {
    private final EventRepository eventRepository;
    private final UserTypeService userTypeService;
    private final ParticipationRepository participationRepository;
    private final NCPStorageService s3Service;
    private final LocationService locationService;
    private final CategoryService categoryService;
    private final TicketService ticketService;
    private final ApplicationEventPublisher publisher;
    private final UserRepository userRepository;
    private final RedissonClient redissonClient;

    private final ConcurrentHashMap<Long, Object> eventLocks = new ConcurrentHashMap<>();

    @Transactional
    public Long createEvent(Long userId, EventRequest.EventCreateRequestDTO request, MultipartFile eventImage) {
        String eventImageUrl = s3Service.uploadFile(eventImage);
        Category category = categoryService.findByName(request.getMediaType());
        Location location = locationService.findOne(request.getLocationId());
        String eventEndTime = toEventEndTime(request.getEventStartTime(), request.getEventProgressTime());
        Integer estimatedPrice = toEstimatedPrice(request.getEventProgressTime(), location.getPricePerHour(),
                request.getMinParticipants());

        Event event = EventConverter.toEvent(request, eventImageUrl, category, location, eventEndTime, estimatedPrice);
        eventRepository.save(event);

        // 2) 호스트 Participation 저장
        Participation hostParticipation = Participation.builder()
                .user(userTypeService.findOne(userId))  // User 엔티티 조회
                .event(event)
                .participateRole(ParticipateRole.HOST)
                .build();
        participationRepository.save(hostParticipation);

        // 호스트 생성 알림
        publisher.publishEvent(new HostNotificationEvent(
                event.getId(),
                userId, // hostId
                HostNotificationType.EVENT_CREATED,
                event.getEventTitle()
        ));
        return event.getId();
    }

    private Integer toEstimatedPrice(Integer eventProgressTime, Integer pricePerHour, Integer minParticipants) {
        int estimatedPrice = pricePerHour * eventProgressTime / minParticipants;
        return (int) (Math.round(estimatedPrice / 1000.0) * 1000);
    }

    /**
     * 이벤트 종료 시간 생성 (시작 시각 + 진행 시간)
     **/
    private String toEventEndTime(String eventStartTime, Integer eventProgressTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime startTime = LocalTime.parse(eventStartTime, formatter);

        LocalTime endTime = startTime.plusHours(eventProgressTime);
        return endTime.format(formatter);
    }

    public Event findOne(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));
    }

    public EventResponse.ReadEventListWithPageResultDTO readEventListByCategory(String category, Integer page,
                                                                                Integer size) {
        Page<Event> eventList;
        switch (category) {
            case "인기":
                eventList = eventRepository.findOrderByPopularity(PageRequest.of(page, size));
                break;
            case "최신":
                eventList = eventRepository.findOrderByCreatedAtDesc(PageRequest.of(page, size));
                break;
            default:
                eventList = eventRepository.findByCategoryName(category, PageRequest.of(page, size));
                break;
        }
        int totalPages = eventList.getTotalPages();
        List<EventResponse.ReadEventListResultDTO> eventListResultDTO = toReadEventListResultDTO(eventList);

        return EventConverter.toReadEventListWithPageResult(totalPages, eventListResultDTO);
    }

    public EventResponse.ReadEventListWithPageResultDTO readEventListBySearch(String content, Integer page,
                                                                              Integer size) {
        Page<Event> eventList = eventRepository.findEventsBySearch(content, PageRequest.of(page, size));
        int totalPages = eventList.getTotalPages();
        List<EventResponse.ReadEventListResultDTO> eventListResultDTO = toReadEventListResultDTO(eventList);

        return EventConverter.toReadEventListWithPageResult(totalPages, eventListResultDTO);
    }

    private List<EventResponse.ReadEventListResultDTO> toReadEventListResultDTO(Page<Event> eventList) {
        return eventList.stream().map(
                e -> {
                    double percentage = ((double) e.getCurrentParticipants() / e.getMaxParticipants()) * 100;
                    int rate = Math.round((float) percentage);

                    int d_day = (int) ChronoUnit.DAYS.between(LocalDate.now(), e.getEventDate());

                    return EventConverter.toEventListResultDTO(e, rate, d_day);
                }
        ).collect(Collectors.toList());
    }

    public EventResponse.EventReadDetailsResultDTO readEventDetails(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));

        /** 현재 유저가 주최자 / 참여자 / 신청x 확인 **/
        int status = (userId != null)
                ? event.getParticipationList().stream()
                .filter(p -> p.getUser().getId() == userId)
                .findFirst()
                .map(p -> p.getParticipateRole().equals(ParticipateRole.HOST) ? 0 : 1)
                .orElse(2)
                : 2;

        String buttonState;
        String userRole;
        switch (status) {
            case 0:
                buttonState = event.getHostEventButtonState().getDescription();
                userRole = "주최자";
                break;
            case 1:
                buttonState = event.getParticipantEventButtonState().getDescription();
                userRole = "참여자";
                break;
            default:
                buttonState = event.getAnonymousButtonState().getDescription();
                userRole = "미참여자";
                break;
        }

        User host = participationRepository.findHostParticipationByEventId(eventId);

        return EventConverter.toEventReadDetailsResultDTO(
                event,
                host.getUsername(),
                host.getProfileImage(),
                host.getHostExperienceCount(),
                userRole,
                formatDateRange(event.getRecruitmentStart(), event.getRecruitmentEnd()),
                Math.round((float) ((double) event.getCurrentParticipants() / event.getMaxParticipants()) * 100),
                buttonState
        );

    }

    /**
     * 이벤트 모집기간 출력 포맷팅
     **/
    private static String formatDateRange(LocalDate startDate, LocalDate endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy. MM. dd");

        String formattedStartDate = startDate.format(formatter);
        String formattedEndDate = endDate.format(formatter);

        return formattedStartDate + " - " + formattedEndDate;
    }

    /**
     * 이벤트 신청 혹은 취소
     **/
//    @Transactional
//    public void registerEvent(Long eventId, Boolean type) {
//        // 참여인원 추가
//        Event event = eventRepository.findByIdWithLock(eventId)
//                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));
//        event.updateCurrentParticipants(type);
//    }

    /**
     * 이벤트 신청
     **/
    @Transactional
    public void registerEvent2(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        // user 조회는 락 밖에서 미리 수행

        RLock lock = redissonClient.getLock("event:" + eventId + ":lock");
        boolean acquired;
        try {
            long start = System.currentTimeMillis();
            // 최대 50ms만 대기, 획득 후 자동 해제(10s)
            acquired = lock.tryLock(50, 10, TimeUnit.SECONDS);
            long waited = System.currentTimeMillis() - start;
            log.info("Lock wait: {}ms", waited);

            if (!acquired) {
                throw new BusinessException(ErrorCode.SYSTEM_BUSY);
            }

            Event event = eventRepository.findByIdWithLock(eventId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));
            if (event.getCurrentParticipants() + 1 > event.getMaxParticipants()) {
                throw new BusinessException(ErrorCode.EVENT_FULL);
            }

            // 카운트 증감 + 저장
            event.updateCurrentParticipants(true);
            Participation p = ParticipationConverter.toParticipation(
                    user, event, ParticipateRole.PARTICIPANT
            );
            participationRepository.save(p);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.SYSTEM_BUSY);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Transactional
    public void registerEvent(Long userId, Long eventId) {
        // 사용자 조회를 락 외부에서 미리 수행
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 이벤트 전용 락 오브젝트 획득
        Object lock = eventLocks.computeIfAbsent(eventId, id -> new Object());
        synchronized (lock) {
            // 1) DB에서 PESSIMISTIC_WRITE 락으로 가져오기
            Event event = eventRepository.findByIdWithLock(eventId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));

            // 2) 최대 인원 초과 검사
            if (event.getCurrentParticipants() + 1 > event.getMaxParticipants()) {
                throw new BusinessException(ErrorCode.EVENT_FULL);
            }

            // 3) 참여자 수 증가
            event.updateCurrentParticipants(true);

            // 4) 참여자 저장
            Participation p = ParticipationConverter.toParticipation(user, event, ParticipateRole.PARTICIPANT);
            participationRepository.save(p);
        }
        // synchronized 블록이 끝난 후 락 오브젝트 제거
        eventLocks.remove(eventId);
    }

    @Transactional
    public void cleanupEventLock(Long eventId) {
        eventLocks.remove(eventId);
    }


    /**
     * 이벤트 신청 취소
     */
    @Transactional
    public void cancelEvent(Long userId, Long eventId) {
        Event event = eventRepository.findByIdWithLock(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));

        Participation p = participationRepository
                .findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPATION_NOT_FOUND));

        participationRepository.delete(p);

        event.updateCurrentParticipants(false);
    }

    /**
     * 이벤트 모집 취소
     **/
    @Transactional
    public String cancelRecruitEvent(Long userId, Long eventId) {
        // 주최자인지 검증
        Participation participation = participationRepository.findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPATION_NOT_FOUND));
        if (participation.getParticipateRole().equals(ParticipateRole.PARTICIPANT)) {
            throw new BusinessException(ErrorCode.PARTICIPATION_NOT_ALLOWED);
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));
        event.recruitCancel();

        publisher.publishEvent(new HostNotificationEvent(
                event.getId(),
                userId, // hostId
                HostNotificationType.RECRUITMENT_CANCELLED,
                event.getEventTitle()
        ));

        // 4) 참여자 전원 조회 → 참여자용 "모집 취소" 알림 발송
        List<Participation> participants = participationRepository
                .findAllByEventIdAndRole(eventId, ParticipateRole.PARTICIPANT);
        for (Participation p : participants) {
            publisher.publishEvent(new ParticipantNotificationEvent(
                    event.getId(),        // ← eventId
                    p.getUser().getId(),
                    ParticipantNotificationType.RECRUITMENT_CANCELLED,
                    event.getEventTitle()
            ));
        }

        return EventConstants.RECRUIT_CANCEL_SUCCESS.getMessage();
    }

    /**
     * 대관 신청 / 취소
     **/
    @Transactional
    public String venueProcess(Long eventId, Integer type) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));

        if (type == 0) {
            event.venueRegister();
            publisher.publishEvent(new VenueRequestWebhookEvent(this, event.getId()));
            return EventConstants.VENUE_RESERVATION_SUCCESS.getMessage();
        } else {
            event.venueCancel();

            // 대관 취소(불가) 시 알림 전송(호스트)
            Long hostId = participationRepository
                    .findByUserIdAndEventIdAndRole(event.getId(), ParticipateRole.HOST)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPATION_NOT_FOUND));

            publisher.publishEvent(new HostNotificationEvent(
                    event.getId(),
                    hostId,
                    HostNotificationType.RESERVATION_DENIED, // 대관 취소 알림 (호스트)
                    event.getEventTitle()
            ));

            // 2) 참여자 전원 대관 취소 알림
            List<Participation> participants = participationRepository
                    .findAllByEventIdAndRole(eventId, ParticipateRole.PARTICIPANT);
            for (Participation p : participants) {
                publisher.publishEvent(new ParticipantNotificationEvent(
                        event.getId(),
                        p.getUser().getId(),
                        ParticipantNotificationType.RESERVATION_NOT_APPLIED, // 대관 취소 알림 (참여자)
                        event.getEventTitle()
                ));
            }

            return EventConstants.VENUE_CANCEL_SUCCESS.getMessage();
        }
    }

    /**
     * 대관 확정
     **/
    @Transactional
    public EventResponse.EventVenueConfirmedResultDTO venueConfirmed(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));

        event.venueConfirmed();
        Long ticketId = ticketService.createTicket(event);// 티켓 생성

        // 호스트 ID 조회
        Long hostId = participationRepository
                .findByUserIdAndEventIdAndRole(event.getId(), ParticipateRole.HOST)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPATION_NOT_FOUND));

        //  호스트에게 대관 확정 알림 발송
        publisher.publishEvent(new HostNotificationEvent(
                event.getId(),
                hostId,
                HostNotificationType.RESERVATION_CONFIRMED, // 대관 확정 알림 (호스트)
                event.getEventTitle()
        ));

        // 참여자 전원에게 대관 확정 알림 발송
        List<Participation> participants = participationRepository
                .findAllByEventIdAndRole(eventId, ParticipateRole.PARTICIPANT);
        for (Participation p : participants) {
            publisher.publishEvent(new ParticipantNotificationEvent(
                    event.getId(),
                    p.getUser().getId(),
                    ParticipantNotificationType.RESERVATION_CONFIRMED, // 대관 확정 알림 (참여자)
                    event.getEventTitle()
            ));
        }

        return EventConverter.toEventVenueConfirmedResultDTO(ticketId);
    }

    /**
     * 상영 완료 혹은 취소
     **/
    @Transactional
    public String screeningProcess(Long userId, Long eventId, Integer type) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));
        event.screeningProcess(type);

        if (type == 0) {
            Participation participation = participationRepository.findByUserIdAndEventId(userId, eventId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPATION_NOT_FOUND));

            boolean isHost = participation.getParticipateRole().equals(ParticipateRole.HOST);
            userTypeService.updateUserExperience(userId, isHost ? 0 : 1);

            // 호스트 조회
            Long hostId = participationRepository
                    .findByUserIdAndEventIdAndRole(eventId, ParticipateRole.HOST)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPATION_NOT_FOUND));

            // 상영 완료 후기 요청 알림(호스트)
            publisher.publishEvent(new HostNotificationEvent(
                    event.getId(),
                    hostId,
                    HostNotificationType.SCREENING_COMPLETED,
                    event.getEventTitle()
            ));

            // 참여자 전원 상영 완료 후기 요청 알림 발송
            List<Participation> participants = participationRepository
                    .findAllByEventIdAndRole(eventId, ParticipateRole.PARTICIPANT);
            for (Participation p : participants) {
                publisher.publishEvent(new ParticipantNotificationEvent(
                        event.getId(),
                        p.getUser().getId(),
                        ParticipantNotificationType.SCREENING_COMPLETED, // 상영 완료 후기 요청 알림 (참여자)
                        event.getEventTitle()
                ));
            }

            return EventConstants.SCREENING_DONE_SUCCESS.getMessage();
        }
        return EventConstants.SCREENING_CANCEL_SUCCESS.getMessage();
    }

    /**
     * 매일 자정에 모집 끝난 이벤트 확인 및 이후 과정 처리 로직
     **/
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    @Transactional
    public void processExpiredEvents() {
        List<Event> expiredEvents = findExpiredEvents();

        expiredEvents.forEach(event -> {
            Long eventId = event.getId();

            // 이벤트를 생성한 호스트의 userId = hostId로 설정 -> 엔티티 조회가 아닌 userId만 조회
            Long hostId = participationRepository
                    .findByUserIdAndEventIdAndRole(event.getId(), ParticipateRole.HOST)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPATION_NOT_FOUND));

            if (event.getCurrentParticipants() < event.getMinParticipants()) {
                event.recruitCancel();
                // 인원부족으로 이벤트 취소 시 자동 알림(호스트)
                publisher.publishEvent(new HostNotificationEvent(
                        event.getId(),
                        hostId,
                        HostNotificationType.RECRUITMENT_CANCELLED, // 인원 부족 취소 (호스트)
                        event.getEventTitle()
                ));

                // 모든 참여자 조회 후 알림 발송
                List<Participation> participants = participationRepository
                        .findAllByEventIdAndRole(eventId, ParticipateRole.PARTICIPANT);
                for (Participation p : participants) {
                    publisher.publishEvent(new ParticipantNotificationEvent(
                            event.getId(),
                            p.getUser().getId(),
                            ParticipantNotificationType.RECRUITMENT_CANCELLED, // 인원 부족 알림 (참여자)
                            event.getEventTitle()
                    ));
                }

            } else {
                event.recruitDone();
                // 인원 모집 달성 상태로 모집 기간 끝날 시 자동으로 알림 발송(호스트)
                publisher.publishEvent(new HostNotificationEvent(
                        event.getId(),
                        hostId,
                        HostNotificationType.RECRUITMENT_COMPLETED, // 모집 완료 알림 (호스트)
                        event.getEventTitle()
                ));

                // 참여자 전원에게 모집 완료 알림 발송
                List<Participation> participants = participationRepository
                        .findAllByEventIdAndRole(eventId, ParticipateRole.PARTICIPANT);
                for (Participation p : participants) {
                    publisher.publishEvent(new ParticipantNotificationEvent(
                            event.getId(),
                            p.getUser().getId(),
                            ParticipantNotificationType.RECRUITMENT_COMPLETED, // 모집 완료 알림 (참여자)
                            event.getEventTitle()
                    ));
                }
            }
        });
    }

    private List<Event> findExpiredEvents() { // 모집 기간이 지난 이벤트 탐색
        return eventRepository.findExpiredEvent(LocalDate.now());
    }

    public List<EventResponse.HomeEventListResultDTO> readHomeEventList(Long userId) {
        UserType userType = userTypeService.findUserType(userId);
        ContentCategory preferCategory = userType.getCategory();
        Long categoryId;
        switch (preferCategory) {
            case MOVIE:
                categoryId = 1L;
                break;
            case DRAMA:
                categoryId = 2L;
                break;
            case SPORTS:
                categoryId = 3L;
                break;
            case VARIETY:
                categoryId = 4L;
                break;
            case CONCERT:
                categoryId = 5L;
                break;
            default:
                throw new BusinessException(ErrorCode.USER_TYPE_NOT_FOUND);
        }

        List<Event> eventList = eventRepository.findEventListByUserType(categoryId, PageRequest.of(0, 7));
        return eventList.stream().map(event -> {
            String eventDate = event.getEventDate().format(DateTimeFormatter.ofPattern("yyyy. MM. dd"));
            return EventConverter.toHomeEventListResultDTO(event, eventDate);
        }).collect(Collectors.toList());
    }

    @Transactional
    public int cancelAllParticipants(Long eventId) {
        eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));

        List<Participation> participants = participationRepository
                .findAllByEventIdAndRole(eventId, ParticipateRole.PARTICIPANT);

        // 일괄 삭제
        participationRepository.deleteAll(participants);

        eventRepository.findById(eventId).ifPresent(event -> event.resetCurrentParticipants());

        return participants.size();
    }

    // 특정 이벤트에 참여 중인 사용자 목록 조회
    public EventParticipantsResponse getEventParticipants(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));

        List<Participation> list = participationRepository
                .findAllByEventIdAndParticipateRole(eventId, ParticipateRole.PARTICIPANT);

        List<ParticipantDTO> dtos = list.stream()
                .map(p -> {
                    var u = p.getUser();
                    return new ParticipantDTO(u.getId(), u.getUsername(), u.getProfileImage());
                })
                .collect(Collectors.toList());

        int totalCount = event.getCurrentParticipants();
        return new EventParticipantsResponse(dtos, totalCount);
    }

}
