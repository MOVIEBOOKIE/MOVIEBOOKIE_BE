package project.luckybooky.domain.admin.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.luckybooky.domain.admin.converter.WebhookConverter;
import project.luckybooky.domain.admin.dto.EventUserInfoWebhookDTO;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.event.repository.EventRepository;
import project.luckybooky.domain.participation.entity.Participation;
import project.luckybooky.domain.participation.entity.type.ParticipateRole;
import project.luckybooky.domain.participation.repository.ParticipationRepository;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@Service
@RequiredArgsConstructor
public class AdminEventUserInfoService {

    private final EventRepository eventRepository;
    private final ParticipationRepository participationRepository;
    private final EventUserInfoWebhookService eventUserInfoWebhookService;

    public void sendEventUserInfoWebhook(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));

        Participation host = participationRepository
                .findFirstByEventIdAndParticipateRoleWithUser(eventId, ParticipateRole.HOST)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPATION_NOT_FOUND));

        List<Participation> participants = participationRepository
                .findAllByEventIdAndParticipateRole(eventId, ParticipateRole.PARTICIPANT);

        EventUserInfoWebhookDTO dto = WebhookConverter.toEventUserInfoDto(event, host, participants);
        eventUserInfoWebhookService.sendEventUserInfo(dto);
    }
}

