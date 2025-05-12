package project.luckybooky.domain.event.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ParticipantEventButtonState {
    REGISTER_CANCELED("신청 취소"),
    RECRUIT_CANCELED("모집 취소"),
    RECRUIT_DONE("모집 완료"),
    VENUE_RESERVATION_IN_PROGRESS("대관 진행 중"),
    VENUE_RESERVATION_CANCELED("대관 취소"),
    TO_TICKET("티켓으로 이동");

    private final String description;

    public static ParticipantEventButtonState fromDescription(String description) {
        for (ParticipantEventButtonState participantEventStatus : ParticipantEventButtonState.values()) {
            if (participantEventStatus.description.equals(description)) {
                return participantEventStatus;
            }
        }
        throw new IllegalArgumentException("Unknown InvoiceCorrectReason description: " + description);
    }
}
