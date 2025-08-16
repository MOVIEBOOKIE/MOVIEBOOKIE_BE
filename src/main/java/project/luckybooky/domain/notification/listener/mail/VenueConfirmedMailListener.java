package project.luckybooky.domain.notification.listener.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.event.entity.type.EventStatus;
import project.luckybooky.domain.event.entity.type.HostEventButtonState;
import project.luckybooky.domain.event.entity.type.ParticipantEventButtonState;
import project.luckybooky.domain.notification.converter.NotificationConverter;
import project.luckybooky.domain.notification.dto.ConfirmedData;
import project.luckybooky.domain.notification.event.app.HostNotificationEvent;
import project.luckybooky.domain.notification.service.MailTemplateService;
import project.luckybooky.domain.participation.entity.Participation;
import project.luckybooky.domain.participation.entity.type.ParticipateRole;
import project.luckybooky.domain.participation.repository.ParticipationRepository;

@Component
@Slf4j
@RequiredArgsConstructor
public class VenueConfirmedMailListener {
    private final ParticipationRepository participationRepository;
    private final MailTemplateService mailTemplateService;

    @Value("${app.home-url}")
    private String homeUrl;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onHostReservationConfirmed(HostNotificationEvent evt) {

        Participation hostPart = participationRepository
                .findByUser_IdAndEvent_IdAndParticipateRole(
                        evt.getHostUserId(), evt.getEventId(), ParticipateRole.HOST
                )
                .orElse(null);
        if (hostPart == null) {
            return;
        }

        Event ev = hostPart.getEvent();

        boolean shouldSend =
                ev.getEventStatus() == EventStatus.VENUE_CONFIRMED
                        && ev.getHostEventButtonState() == HostEventButtonState.TO_TICKET
                        && ev.getParticipantEventButtonState() == ParticipantEventButtonState.TO_TICKET;

        if (!shouldSend) {
            return;
        }

        ConfirmedData data = NotificationConverter.toConfirmedData(hostPart, homeUrl);
        String to = hostPart.getUser().getCertificationEmail();

        mailTemplateService.sendVenueConfirmedMail(to, data);
        log.info("✅ [메일] 대관확정 발송 완료: to={}, eventId={}", to, ev.getId());
    }
}
