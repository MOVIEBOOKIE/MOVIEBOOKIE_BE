package project.luckybooky.domain.participation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.luckybooky.domain.event.converter.EventConverter;
import project.luckybooky.domain.event.dto.response.EventResponse;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.event.entity.type.EventStatus;
import project.luckybooky.domain.event.service.EventService;
import project.luckybooky.domain.participation.converter.ParticipationConverter;
import project.luckybooky.domain.participation.entity.Participation;
import project.luckybooky.domain.participation.entity.type.ParticipateRole;
import project.luckybooky.domain.participation.repository.ParticipationRepository;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.repository.UserRepository;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationService {
    private final ParticipationRepository participationRepository;
    private final UserRepository userRepository;
    private final EventService eventService;

    @Transactional
    public EventResponse.EventCreateResultDTO createParticipation(Long userId, Long eventId, Boolean isHost) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Event event = eventService.findOne(eventId);
        ParticipateRole role = isHost ? ParticipateRole.HOST : ParticipateRole.PARTICIPANT;

        Participation participation = ParticipationConverter.toParticipation(user, event, role);
        participationRepository.save(participation);
        return EventConverter.toEventCreateResponseDTO(event);
    }

    @Transactional
    public void deleteParticipation(Long userId, Long eventId) {
        participationRepository.deleteByUserIdAndEventId(userId, eventId);
    }

    public List<EventResponse.ReadEventListResultDTO> readEventList(Long userId, Integer type, Integer role, Integer page, Integer size) {
        // 진행 중, 확정 이벤트 목록 필터링
        List<EventStatus> statuses = (type == 0)
                ? List.of(
                EventStatus.RECRUITING,
                EventStatus.RECRUITED,
                EventStatus.VENUE_RESERVATION_IN_PROGRESS
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
        Page<Event> eventList = participationRepository.findByUserIdAndEventStatuses(userId, participateRole, statuses, PageRequest.of(page, size));

        return toReadEventListResultDTO(eventList);
    }

    private List<EventResponse.ReadEventListResultDTO> toReadEventListResultDTO(Page<Event> eventList) {
        return eventList.stream().map(
                e -> {
                    double percentage = ((double) e.getCurrentParticipants() / e.getMaxParticipants()) * 100;
                    int rate = Math.round((float) percentage);
                    return EventConverter.toEventListResultDTO(e, rate, -1);
                }
        ).collect(Collectors.toList());
    }
}
