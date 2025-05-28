package project.luckybooky.domain.admin.converter;

import static project.luckybooky.domain.participation.entity.type.ParticipateRole.HOST;
import static project.luckybooky.domain.participation.entity.type.ParticipateRole.PARTICIPANT;

import java.time.format.DateTimeFormatter;
import java.util.List;
import project.luckybooky.domain.admin.dto.ParticipantInfo;
import project.luckybooky.domain.admin.dto.VenueRequestWebhookDTO;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.participation.entity.Participation;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

public class WebhookConverter {

    public static VenueRequestWebhookDTO toDto(Event event, List<Participation> participations) {
        // 날짜, 시간, 장소
        String date = event.getEventDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        String time = event.getEventStartTime() + "-" + event.getEventEndTime();
        String location = event.getLocation().getLocationName();

        // 호스트 찾기 (없으면 예외)
        Participation hostParticipation = participations.stream()
                .filter(p -> p.getParticipateRole() == HOST)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPATION_NOT_FOUND));
        project.luckybooky.domain.user.entity.User hostUser = hostParticipation.getUser();

        // 참여자 정보 리스트
        List<ParticipantInfo> participantInfos = participations.stream()
                .filter(p -> p.getParticipateRole() == PARTICIPANT)
                .map(p -> {
                    project.luckybooky.domain.user.entity.User u = p.getUser();
                    return new ParticipantInfo(
                            u.getCertificationEmail(),
                            u.getUsername(),
                            u.getPhoneNumber(),
                            u.getUserType().getTitle()  // UserType의 title 필드
                    );
                })
                .toList();

        // 목적(purpose) 필드: 이벤트 설명을 사용
        String purpose = event.getDescription();

        return new VenueRequestWebhookDTO(
                date,
                time,
                location,
                hostUser.getUsername(),
                hostUser.getPhoneNumber(),
                participantInfos.size(),
                purpose,
                participantInfos
        );
    }
}
