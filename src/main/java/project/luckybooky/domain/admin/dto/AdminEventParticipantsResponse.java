package project.luckybooky.domain.admin.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import project.luckybooky.domain.participation.dto.ParticipantInfoDto;

@Getter
@Builder
public class AdminEventParticipantsResponse {
    private final List<ParticipantInfoDto> participants;
    private final Integer totalParticipantsCount;
}
