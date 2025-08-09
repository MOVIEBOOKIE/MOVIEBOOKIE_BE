package project.luckybooky.domain.participation.dto;

import lombok.Getter;
import project.luckybooky.domain.event.entity.Event;
import java.util.List;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class ParticipantInfoResult {
    private final Event event;
    private final List<ParticipantInfoDto> participants;

}
