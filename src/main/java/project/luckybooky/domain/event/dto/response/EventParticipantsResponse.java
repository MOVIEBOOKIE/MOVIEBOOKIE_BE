package project.luckybooky.domain.event.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventParticipantsResponse {
    private List<ParticipantDTO> participants;
    private long totalParticipantsCount;
}
