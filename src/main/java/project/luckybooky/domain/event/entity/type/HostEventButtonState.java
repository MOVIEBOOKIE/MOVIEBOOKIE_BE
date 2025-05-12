package project.luckybooky.domain.event.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum HostEventButtonState {
    RECRUIT_CANCELLED("모집 취소"),
    VENUE_RESERVATION("대관 신청하기"),
    VENUE_RESERVATION_IN_PROGRESS("대관 진행 중"),
    VENUE_RESERVATION_CANCELED("대관 취소"),
    TO_TICKET("티켓으로 이동");

    private final String description;

    public static HostEventButtonState fromDescription(String description) {
        for (HostEventButtonState hostEventButtonState : HostEventButtonState.values()) {
            if (hostEventButtonState.description.equals(description)) {
                return hostEventButtonState;
            }
        }
        throw new IllegalArgumentException("Unknown InvoiceCorrectReason description: " + description);
    }
}
