package project.luckybooky.domain.notification.listener.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.event.entity.type.EventStatus;
import project.luckybooky.domain.event.entity.type.HostEventButtonState;
import project.luckybooky.domain.event.entity.type.ParticipantEventButtonState;
import project.luckybooky.domain.notification.converter.NotificationConverter;
import project.luckybooky.domain.notification.dto.RejectedData;
import project.luckybooky.domain.notification.event.app.HostNotificationEvent;
import project.luckybooky.domain.notification.service.MailTemplateService;
import project.luckybooky.domain.participation.entity.Participation;
import project.luckybooky.domain.participation.entity.type.ParticipateRole;
import project.luckybooky.domain.participation.repository.ParticipationRepository;

@Component
@Slf4j
@RequiredArgsConstructor
public class VenueRejectedMailListener {

    private final ParticipationRepository participationRepository;
    private final MailTemplateService mailTemplateService;

    @Value("${app.home-url}")
    private String homeUrl;

    @EventListener
    public void sendVenueRejectedMail(HostNotificationEvent evt) {

        Participation hostPart = participationRepository
                .findByUser_IdAndEvent_IdAndParticipateRole(
                        evt.getHostUserId(), evt.getEventId(), ParticipateRole.HOST
                )
                .orElse(null);
        if (hostPart == null) {
            return;
        }

        Event ev = hostPart.getEvent();

        // ✅ 조건이 맞을 때만 발송
        boolean shouldSend =
                ev.getEventStatus() == EventStatus.VENUE_RESERVATION_CANCELED
                        && ev.getHostEventButtonState() == HostEventButtonState.VENUE_RESERVATION_CANCELED
                        && ev.getParticipantEventButtonState()
                        == ParticipantEventButtonState.VENUE_RESERVATION_CANCELED;

        if (!shouldSend) {
            return;
        }

        RejectedData data = NotificationConverter.toRejectedData(hostPart, homeUrl);
        String to = hostPart.getUser().getCertificationEmail();
        mailTemplateService.sendVenueRejectedMail(to, data);
        log.info("✅ [메일] 대관취소 발송 완료: to={}, eventId={}", to, ev.getId());
    }
}

