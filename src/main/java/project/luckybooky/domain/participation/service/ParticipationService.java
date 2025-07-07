package project.luckybooky.domain.participation.service;

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
import project.luckybooky.domain.notification.event.ParticipantNotificationEvent;
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
        // 진행 중, 확정 이벤트 목록 필터링
        List<EventStatus> statuses = (type == 0)
                ? List.of(
                EventStatus.RECRUITING,
                EventStatus.RECRUITED
        )
                : List.of(
                        EventStatus.COMPLETED,
                        EventStatus.CANCELLED,
                        EventStatus.VENUE_CONFIRMED,
                        EventStatus.RECRUIT_CANCELED,
                        EventStatus.VENUE_RESERVATION_CANCELED
                );

        // 주최자 / 참여자 판단
        ParticipateRole participateRole = (role == 0) ? ParticipateRole.PARTICIPANT : ParticipateRole.HOST;
        Page<Event> eventList = participationRepository.findByUserIdAndEventStatuses(userId, participateRole, statuses,
                PageRequest.of(page, size));

        return toReadEventListResultDTO(eventList);
    }

    private List<EventResponse.ReadEventListResultDTO> toReadEventListResultDTO(Page<Event> eventList) {
        return eventList.stream().map(
                e -> {
                    return EventConverter.toEventListResultDTO(e);
                }
        ).collect(Collectors.toList());
    }
}
