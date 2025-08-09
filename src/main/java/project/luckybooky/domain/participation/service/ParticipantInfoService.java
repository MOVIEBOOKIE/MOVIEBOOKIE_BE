package project.luckybooky.domain.participation.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.event.repository.EventRepository;
import project.luckybooky.domain.participation.dto.ParticipantInfoDto;
import project.luckybooky.domain.participation.dto.ParticipantInfoResult;
import project.luckybooky.domain.participation.entity.type.ParticipateRole;
import project.luckybooky.domain.participation.repository.ParticipationRepository;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@Service
@RequiredArgsConstructor
public class ParticipantInfoService {
    private final ParticipationRepository participationRepository;
    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public List<ParticipantInfoDto> getParticipantInfo(Long eventId) {
        return participationRepository
                .findAllByEventIdAndRole(eventId, ParticipateRole.PARTICIPANT)
                .stream()
                .map(p -> new ParticipantInfoDto(
                        p.getUser().getUsername(),
                        p.getUser().getPhoneNumber()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 주최자 권한 확인 및 이벤트 정보 조회
     */
    @Transactional(readOnly = true)
    public Event validateHostAndGetEvent(Long eventId, Long userId) {
        // 이벤트 존재 확인
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));

        // 현재 사용자가 해당 이벤트의 주최자인지 확인
        participationRepository
                .findByUser_IdAndEvent_IdAndParticipateRole(userId, eventId, ParticipateRole.HOST)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPATION_NOT_ALLOWED));

        return event;
    }

    /**
     * 주최자 권한 확인 및 참여자 정보와 이벤트 정보를 함께 조회
     */
    @Transactional(readOnly = true)
    public ParticipantInfoResult getParticipantInfoForHost(Long eventId, Long userId) {
        // 이벤트 존재 확인
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));

        // 현재 사용자가 해당 이벤트의 주최자인지 확인
        participationRepository
                .findByUser_IdAndEvent_IdAndParticipateRole(userId, eventId, ParticipateRole.HOST)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPATION_NOT_ALLOWED));

        // 참여자 정보 조회
        List<ParticipantInfoDto> participants = getParticipantInfo(eventId);

        return new ParticipantInfoResult(event, participants);
    }

    /**
     * 이벤트 날짜 포맷팅
     */
    public String formatEventDate(Event event) {
        return event.getEventDate()
                .format(DateTimeFormatter.ofPattern("yyyy년 M월 dd일"));
    }
}
