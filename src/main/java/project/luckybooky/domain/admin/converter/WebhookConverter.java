package project.luckybooky.domain.admin.converter;

import java.time.format.DateTimeFormatter;
import java.util.List;
import project.luckybooky.domain.admin.dto.EventCreatedWebhookDTO;
import project.luckybooky.domain.admin.dto.EventUserInfoWebhookDTO;
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
        String date = event.getEventDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        String time = event.getEventStartTime() + "-" + event.getEventEndTime();
        String location = event.getLocation().getLocationName();

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
                    return ParticipantInfo.builder()
                            .certificationEmail(u.getCertificationEmail())
                            .username(u.getUsername())
                            .phoneNumber(u.getPhoneNumber())
                            .userTypeTitle(u.getUserType().getTitle())
                            .build();
                })
                .toList();

        String purpose = event.getDescription();

        return VenueRequestWebhookDTO.builder()
                .date(date)
                .time(time)
                .locationName(location)
                .hostUsername(hostName)
                .hostPhoneNumber(hostPhone)
                .participantCount(participantInfos.size())
                .purpose(purpose)
                .participants(participantInfos)
                .build();
    }

    public static EventCreatedWebhookDTO toEventCreatedDto(
            Event event,
            Participation hostParticipation
    ) {
        String date = event.getEventDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        String time = event.getEventStartTime() + "-" + event.getEventEndTime();
        String location = event.getLocation().getLocationName();

        if (hostParticipation == null) {
            throw new BusinessException(ErrorCode.PARTICIPATION_NOT_FOUND);
        }
        var hostUser = hostParticipation.getUser();
        String hostName = hostUser.getUsername();
        String hostPhone = hostUser.getPhoneNumber();

        String recruitmentPeriod = event.getRecruitmentStart().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                + " - " + event.getRecruitmentEnd().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));

        return EventCreatedWebhookDTO.builder()
                .eventTitle(event.getEventTitle())
                .mediaTitle(event.getMediaTitle())
                .date(date)
                .time(time)
                .locationName(location)
                .hostUsername(hostName)
                .hostPhoneNumber(hostPhone)
                .minParticipants(event.getMinParticipants())
                .maxParticipants(event.getMaxParticipants())
                .description(event.getDescription())
                .categoryName(event.getCategory().getCategoryName())
                .recruitmentPeriod(recruitmentPeriod)
                .estimatedPrice(event.getEstimatedPrice())
                .build();
    }

    public static EventUserInfoWebhookDTO toEventUserInfoDto(
            Event event,
            Participation hostParticipation,
            List<Participation> participantList
    ) {
        String date = event.getEventDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));

        String hostName = "";
        if (hostParticipation != null) {
            hostName = hostParticipation.getUser().getUsername();
        }

        List<EventUserInfoWebhookDTO.EventUserInfoDetail> participantInfos = participantList.stream()
                .map(p -> {
                    var u = p.getUser();
                    return EventUserInfoWebhookDTO.EventUserInfoDetail.builder()
                            .username(u.getUsername())
                            .phoneNumber(u.getPhoneNumber())
                            .certificationEmail(u.getCertificationEmail())
                            .groupType(u.getGroupType() != null ? u.getGroupType().name() : "N/A")
                            .build();
                })
                .toList();

        return EventUserInfoWebhookDTO.builder()
                .eventTitle(event.getEventTitle())
                .date(date)
                .hostUsername(hostName)
                .participantCount(participantInfos.size())
                .participants(participantInfos)
                .build();
    }
}
