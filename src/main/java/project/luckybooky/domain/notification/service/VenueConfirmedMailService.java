package project.luckybooky.domain.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.notification.converter.NotificationConverter;
import project.luckybooky.domain.notification.dto.ConfirmedData;
import project.luckybooky.domain.notification.dto.request.SendVenueConfirmedMailRequestDTO;
import project.luckybooky.domain.participation.entity.Participation;
import project.luckybooky.domain.participation.entity.type.ParticipateRole;
import project.luckybooky.domain.participation.repository.ParticipationRepository;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@Service
@RequiredArgsConstructor
public class VenueConfirmedMailService {

    private final ParticipationRepository participationRepository;
    private final MailTemplateService mailTemplateService;

    /**
     * eventId 기준으로 호스트의 certificationEmail로 대관 확정 메일 전송
     */
    @Transactional(readOnly = true)
    public void sendVenueConfirmedMailToHost(Long eventId, SendVenueConfirmedMailRequestDTO req) {
        Participation hostPart = participationRepository
                .findFirstByEventIdAndParticipateRole(eventId, ParticipateRole.HOST)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_HOST_NOT_FOUND)); // 의미 명확화

        Event event = hostPart.getEvent();
        if (event == null) {
            throw new BusinessException(ErrorCode.EVENT_NOT_FOUND);
        }

        // 2) 호스트 인증 이메일
        String to = hostPart.getUser().getCertificationEmail();
        if (to == null || to.isBlank()) {
            throw new BusinessException(ErrorCode.HOST_CERTIFICATION_EMAIL_NOT_FOUND);
        }

        ConfirmedData data = NotificationConverter.toConfirmedData(hostPart, null);

        mailTemplateService.sendVenueConfirmedMailCustom(
                to,
                data,
                req != null ? req.getCompanyName() : null,
                req != null ? req.getContactInfo() : null,
                req != null ? req.getAccountAndNotes() : null
        );
    }
}
