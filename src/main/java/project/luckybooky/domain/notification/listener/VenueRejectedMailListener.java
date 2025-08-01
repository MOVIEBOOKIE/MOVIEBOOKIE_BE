package project.luckybooky.domain.notification.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import project.luckybooky.domain.notification.dto.ConfirmedData;
import project.luckybooky.domain.notification.event.HostNotificationEvent;
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
public class VenueRejectedMailListener {

    private final ParticipationRepository participationRepository;
    private final MailTemplateService mailTemplateService;

    @EventListener
    public void onHostReservationDenied(HostNotificationEvent evt) {
        if (evt.getType() != HostNotificationType.RESERVATION_DENIED) {
            return;
        }

        Participation hostPart = participationRepository
                .findByUser_IdAndEvent_IdAndParticipateRole(evt.getHostUserId(), evt.getEventId(), ParticipateRole.HOST)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPATION_NOT_FOUND));

        String hostEmail = hostPart.getUser().getCertificationEmail();
        String hostName = hostPart.getUser().getUsername() != null ? hostPart.getUser().getUsername() : "주최자님";
        String eventTitle = hostPart.getEvent().getEventTitle();

        ConfirmedData data = ConfirmedData.builder()
                .eventTitle(eventTitle)
                .hostName(hostName)
                .build();

        mailTemplateService.sendVenueRejectedMail(hostEmail, data);
    }
}
