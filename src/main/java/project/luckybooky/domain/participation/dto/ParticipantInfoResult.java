package project.luckybooky.domain.participation.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ParticipantInfoResult {
    private final List<ParticipantInfoDto> participants;
    private final String viewDate;

}
