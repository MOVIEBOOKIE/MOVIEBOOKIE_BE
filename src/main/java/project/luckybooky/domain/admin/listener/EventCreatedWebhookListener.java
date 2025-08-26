package project.luckybooky.domain.admin.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import project.luckybooky.domain.admin.converter.WebhookConverter;
import project.luckybooky.domain.admin.dto.EventCreatedWebhookDTO;
import project.luckybooky.domain.admin.event.EventCreatedWebhookEvent;
import project.luckybooky.domain.admin.service.EventCreatedWebhookService;
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
public class EventCreatedWebhookListener {
    private final EventRepository eventRepository;
    private final ParticipationRepository participationRepository;
    private final EventCreatedWebhookService eventCreatedWebhookService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEventCreated(EventCreatedWebhookEvent evt) {
        Long eventId = evt.getEventId();

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));

        Participation host = participationRepository
                .findFirstByEventIdAndParticipateRole(eventId, ParticipateRole.HOST)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPATION_NOT_FOUND));

        EventCreatedWebhookDTO dto = WebhookConverter.toEventCreatedDto(event, host);

        log.info("▶️ Sending Discord webhook for event creation eventId={}", eventId);
        eventCreatedWebhookService.sendEventCreated(dto);
    }
}
