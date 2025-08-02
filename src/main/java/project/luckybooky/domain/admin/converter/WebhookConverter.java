package project.luckybooky.domain.admin.converter;

import java.time.format.DateTimeFormatter;
import java.util.List;
import project.luckybooky.domain.admin.dto.ParticipantInfo;
import project.luckybooky.domain.admin.dto.VenueRequestWebhookDTO;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.participation.entity.Participation;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

public class WebhookConverter {
    
    public static VenueRequestWebhookDTO toDto(
            Event event,
            Participation hostParticipation,
            List<Participation> participantList
    ) {
        // 날짜, 시간, 장소
        String date = event.getEventDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        String time = event.getEventStartTime() + "-" + event.getEventEndTime();
        String location = event.getLocation().getLocationName();

        // 호스트 정보 검증
        if (hostParticipation == null) {
            throw new BusinessException(ErrorCode.PARTICIPATION_NOT_FOUND);
        }
        var hostUser = hostParticipation.getUser();
        String hostName = hostUser.getUsername();
        String hostPhone = hostUser.getPhoneNumber();

        // 참여자 정보 리스트
        List<ParticipantInfo> participantInfos = participantList.stream()
                .map(p -> {
                    var u = p.getUser();
                    return new ParticipantInfo(
                            u.getCertificationEmail(),
                            u.getUsername(),
                            u.getPhoneNumber(),
                            u.getUserType().getTitle()
                    );
                })
                .toList();

        String purpose = event.getDescription();

        return new VenueRequestWebhookDTO(
                date,
                time,
                location,
                hostName,
                hostPhone,
                participantInfos.size(),
                purpose,
                participantInfos
        );
    }
}