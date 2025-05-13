package project.luckybooky.domain.participation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.luckybooky.domain.event.converter.EventConverter;
import project.luckybooky.domain.event.dto.response.EventResponse;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.event.service.EventService;
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
}
