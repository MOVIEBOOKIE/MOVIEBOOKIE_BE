package project.luckybooky.domain.admin.listener;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import project.luckybooky.domain.admin.converter.WebhookConverter;
import project.luckybooky.domain.admin.dto.VenueRequestWebhookDTO;
import project.luckybooky.domain.admin.event.VenueRequestWebhookEvent;
import project.luckybooky.domain.admin.service.WebhookService;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.event.repository.EventRepository;
import project.luckybooky.domain.participation.entity.Participation;
import project.luckybooky.domain.participation.entity.type.ParticipateRole;
import project.luckybooky.domain.participation.repository.ParticipationRepository;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@Component
@Slf4j
@RequiredArgsConstructor
public class VenueRequestWebhookListener {
    private final EventRepository eventRepository;
    private final ParticipationRepository participationRepository;
    private final WebhookService webhookService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVenueRequest(VenueRequestWebhookEvent evt) {
        Long eventId = evt.getEventId();

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));

        Participation host = participationRepository
                .findFirstByEventIdAndParticipateRole(eventId, ParticipateRole.HOST)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPATION_NOT_FOUND));
        
        List<Participation> participants = participationRepository
                .findAllByEventIdAndParticipateRole(eventId, ParticipateRole.PARTICIPANT);

        VenueRequestWebhookDTO dto = WebhookConverter.toDto(event, host, participants);

        log.info("▶️ Sending Discord webhook for venue request eventId={}", eventId);
        webhookService.sendVenueRequest(dto);
    }
}
