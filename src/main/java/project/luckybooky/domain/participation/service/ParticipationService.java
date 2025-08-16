package project.luckybooky.domain.participation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.luckybooky.domain.event.converter.EventConverter;
import project.luckybooky.domain.event.dto.response.EventResponse;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.event.entity.type.EventStatus;
import project.luckybooky.domain.event.service.EventService;
import project.luckybooky.domain.notification.event.app.ParticipantNotificationEvent;
import project.luckybooky.domain.notification.type.ParticipantNotificationType;
import project.luckybooky.domain.participation.converter.ParticipationConverter;
import project.luckybooky.domain.participation.entity.Participation;
import project.luckybooky.domain.participation.entity.type.ParticipateRole;
import project.luckybooky.domain.participation.repository.ParticipationRepository;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.repository.UserRepository;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationService {
    private final ParticipationRepository participationRepository;
    private final UserRepository userRepository;
    private final EventService eventService;
    private final ApplicationEventPublisher publisher;


    @Transactional
    public EventResponse.EventCreateResultDTO createParticipation(Long userId, Long eventId, Boolean isHost) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Event event = eventService.findOne(eventId);
        ParticipateRole role = isHost ? ParticipateRole.HOST : ParticipateRole.PARTICIPANT;

        Participation participation = ParticipationConverter.toParticipation(user, event, role);
        participationRepository.save(participation);

        // 참여자인 경우 알림 전송
        if (role == ParticipateRole.PARTICIPANT) {
            publisher.publishEvent(new ParticipantNotificationEvent(
                    eventId,
                    userId,
                    ParticipantNotificationType.APPLY_COMPLETED,
                    event.getEventTitle()
            ));
        }

        return EventConverter.toEventCreateResponseDTO(event);
    }

    public List<EventResponse.ReadEventListResultDTO> readEventList(Long userId, Integer type, Integer role,
                                                                    Integer page, Integer size) {

        // 주최자 / 참여자 판단
        ParticipateRole participateRole = (role == 0) ? ParticipateRole.PARTICIPANT : ParticipateRole.HOST;

        // 진행 중, 확정 이벤트 목록 필터링
        List<EventStatus> statuses;
        Page<Event> eventList;
        if (type == 0) {
            statuses = List.of(
                    EventStatus.RECRUITING,
                    EventStatus.RECRUIT_DONE);

            eventList = participationRepository.findByUserIdAndEventStatusesType1(userId, participateRole, statuses,
                    PageRequest.of(page, size));
        } else if (type == 1) {
            statuses = List.of(
                    EventStatus.VENUE_RESERVATION_IN_PROGRESS,
                    EventStatus.VENUE_CONFIRMED,
                    EventStatus.COMPLETED);

            eventList = participationRepository.findByUserIdAndEventStatusesType2(userId, participateRole, statuses,
                    PageRequest.of(page, size));
        } else {
            statuses = List.of(
                    EventStatus.RECRUIT_CANCELED,
                    EventStatus.VENUE_RESERVATION_CANCELED,
                    EventStatus.CANCELLED
            );
            eventList = participationRepository.findByUserIdAndEventStatusesType3(userId, participateRole, statuses,
                    PageRequest.of(page, size));
        }
        return toReadEventListResultDTO(eventList);
    }

    private List<EventResponse.ReadEventListResultDTO> toReadEventListResultDTO(Page<Event> eventList) {
        return eventList.stream().map(
                e -> {
                    return EventConverter.toEventListResultDTO(e);
                }
        ).collect(Collectors.toList());
    }

    /**
     * 회원 탈퇴 시 연관된 이벤트 취소
     **/
    @Transactional
    public String cancelParticipation(Long userId) {
        List<Participation> participationList = participationRepository.findByUserId(userId);
        participationList.stream().forEach(p -> {
            Event event = p.getEvent();
            if (p.getParticipateRole().equals(ParticipateRole.HOST)) {
                switch (event.getHostEventButtonState()) {
                    case RECRUIT_CANCELED:
                        eventService.cancelRecruitEvent(userId, event.getId());
                        break;
                    case VENUE_RESERVATION, VENUE_RESERVATION_IN_PROGRESS:
                        eventService.venueProcess(userId, event.getId(), 1);
                        break;
                    case TO_TICKET:
                        if (event.getEventStatus().equals(EventStatus.VENUE_CONFIRMED)) {
                            throw new BusinessException(ErrorCode.EVENT_IN_PROGRESS);
                        }
                        break;
                    default:
                        break;
                }
            } else {
                switch (event.getParticipantEventButtonState()) {
                    case REGISTER_CANCELED:
                        eventService.cancelEvent(userId, event.getId());
                        break;
                    case RECRUIT_DONE, VENUE_RESERVATION_IN_PROGRESS:
                        throw new BusinessException(ErrorCode.EVENT_IN_PROGRESS);
                    case TO_TICKET:
                        if (event.getEventStatus().equals(EventStatus.VENUE_CONFIRMED)) {
                            throw new BusinessException(ErrorCode.EVENT_IN_PROGRESS);
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        return "정상 처리되었습니다.";
    }

    /**
     * 해당 날짜에 이벤트 모집 가능 여부 체크
     **/
    public String isRecruitableOnDate(Long userId, String date) {
        boolean result = participationRepository.existsByUserIdAndEventDate(userId, LocalDate.parse(date));
        return result ? "FALSE" : "TRUE";
    }

}
