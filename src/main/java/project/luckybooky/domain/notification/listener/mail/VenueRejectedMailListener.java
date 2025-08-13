package project.luckybooky.domain.notification.listener.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import project.luckybooky.domain.notification.converter.NotificationConverter;
import project.luckybooky.domain.notification.dto.RejectedData;
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
public class VenueRejectedMailListener {

    private final ParticipationRepository participationRepository;
    private final MailTemplateService mailTemplateService;

    @Value("${app.home-url}")
    private String homeUrl;

    @EventListener
    public void sendVenueRejectedMail(HostNotificationEvent evt) {

        if (evt.getType() != HostNotificationType.RESERVATION_DENIED) {
            return;
        }

        Participation hostPart = participationRepository
                .findByUser_IdAndEvent_IdAndParticipateRole(
                        evt.getHostUserId(),
                        evt.getEventId(),
                        ParticipateRole.HOST
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPATION_NOT_FOUND));

        // 2) DTO 변환 로직 위임
        RejectedData data = NotificationConverter.toRejectedData(hostPart, homeUrl);

        // 3) 메일 발송
        String to = hostPart.getUser().getCertificationEmail();
        mailTemplateService.sendVenueRejectedMail(to, data);

        log.info("✅ 대관거절 메일 발송 완료: hostEmail={}, eventId={}", to, evt.getEventId());
    }
}

