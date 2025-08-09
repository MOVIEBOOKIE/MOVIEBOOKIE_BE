package project.luckybooky.domain.participation.dto;

import lombok.Getter;
import project.luckybooky.domain.event.entity.Event;
import java.util.List;

/**
 * 참여자 정보와 이벤트 정보를 담는 결과 클래스
 */
@Getter
public class ParticipantInfoResult {
    private final Event event;
    private final List<ParticipantInfoDto> participants;

    public ParticipantInfoResult(Event event, List<ParticipantInfoDto> participants) {
        this.event = event;
        this.participants = participants;
    }
}
