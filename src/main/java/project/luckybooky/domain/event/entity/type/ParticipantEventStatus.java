package project.luckybooky.domain.event.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ParticipantEventStatus {
    RECRUITING("모집 중"),
    RECRUIT_CANCELLED("모집 취소"),
    BOTH_AVAILABLE("신청/취소 둘다 가능"),
    SCREENING_CANCELLED("상영 취소"),
    CANCELLED_NOT_ALLOWED("신청 취소 불가"),
    SCREENING_COMPLETED("상영 완료"),
    NOT_SCREENED("상영 안함");

    private final String description;

    public static ParticipantEventStatus fromDescription(String description) {
        for (ParticipantEventStatus participantEventStatus : ParticipantEventStatus.values()) {
            if (participantEventStatus.description.equals(description)) {
                return participantEventStatus;
            }
        }
        throw new IllegalArgumentException("Unknown InvoiceCorrectReason description: " + description);
    }
}
