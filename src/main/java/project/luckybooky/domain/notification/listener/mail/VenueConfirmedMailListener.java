package project.luckybooky.domain.notification.listener.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.notification.converter.NotificationConverter;
import project.luckybooky.domain.notification.dto.ConfirmedData;
import project.luckybooky.domain.notification.event.app.HostNotificationEvent;
import project.luckybooky.domain.notification.service.MailTemplateService;
import project.luckybooky.domain.notification.type.HostNotificationType;
import project.luckybooky.domain.participation.entity.Participation;
import project.luckybooky.domain.participation.entity.type.ParticipateRole;
import project.luckybooky.domain.participation.repository.ParticipationRepository;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@Component
@Slf4j
@RequiredArgsConstructor
public class VenueConfirmedMailListener {
    private final ParticipationRepository participationRepository;
    private final MailTemplateService mailTemplateService;

    @Value("${app.home-url}")
    private String homeUrl;

    @EventListener
    public void onHostReservationConfirmed(HostNotificationEvent evt) {

        if (evt.getType() != HostNotificationType.RESERVATION_CONFIRMED) {
            return;
        }

        Participation hostPart = participationRepository
                .findByUser_IdAndEvent_IdAndParticipateRole(evt.getHostUserId(), evt.getEventId(), ParticipateRole.HOST)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPATION_NOT_FOUND));

        Event ev = hostPart.getEvent();

        ConfirmedData data = NotificationConverter.toConfirmedData(hostPart, homeUrl);

        mailTemplateService.sendVenueConfirmedMail(
                hostPart.getUser().getCertificationEmail(),
                data
        );

        log.info("✅ 호스트({})에게 대관확정 메일 발송 완료, eventId={}", hostPart.getUser().getCertificationEmail(), ev.getId());
    }
}
