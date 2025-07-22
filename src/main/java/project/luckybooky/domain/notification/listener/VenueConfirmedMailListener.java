package project.luckybooky.domain.notification.listener;

import java.time.format.TextStyle;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import project.luckybooky.domain.event.entity.Event;
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
public class VenueConfirmedMailListener {
    private final ParticipationRepository participationRepository;
    private final MailTemplateService mailTemplateService;

    @EventListener
    public void onHostReservationConfirmed(HostNotificationEvent evt) {

        if (evt.getType() != HostNotificationType.RESERVATION_CONFIRMED) {
            return;
        }

        // 2) 호스트 participation, event 조회
        Participation hostPart = participationRepository
                .findByUser_IdAndEvent_IdAndParticipateRole(evt.getHostUserId(), evt.getEventId(), ParticipateRole.HOST)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPATION_NOT_FOUND));

        Event ev = hostPart.getEvent();

        // 3) 메일 DTO 빌드
        ConfirmedData data = ConfirmedData.builder()
                .mediaTitle(ev.getMediaTitle())
                .eventTitle(ev.getEventTitle())
                .eventDate(ev.getEventDate())
                .eventDay(ev.getEventDate()
                        .getDayOfWeek()
                        .getDisplayName(TextStyle.SHORT, Locale.KOREAN))
                .eventStartTime(ev.getEventStartTime())
                .eventEndTime(ev.getEventEndTime())
                .locationName(ev.getLocation().getLocationName())
                .maxParticipants(ev.getMaxParticipants())
                .contact("")  // 필요 시 채워주세요
                .participantsLink("https://your-domain.com/events/" + ev.getId() + "/participants")
                .build();

        // 4) 메일 발송
        mailTemplateService.sendVenueConfirmedMail(
                hostPart.getUser().getEmail(),
                data
        );

        log.info("✅ 호스트({})에게 대관확정 메일 발송 완료, eventId={}", hostPart.getUser().getEmail(), ev.getId());
    }
}
