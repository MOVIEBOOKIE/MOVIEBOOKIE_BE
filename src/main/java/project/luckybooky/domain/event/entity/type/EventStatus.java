package project.luckybooky.domain.event.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum EventStatus {
    RECRUITING("모집 중"),
    RECRUITED("모집 완료"),
    VENUE_RESERVATION_IN_PROGRESS("대관 진행 중"),
    COMPLETED("상영 완료"),
    CANCELLED("상영 취소"),
    VENUE_CONFIRMED("대관 확정"),
    RECRUIT_CANCELED("모집 취소"),
    VENUE_RESERVATION_CANCELED("대관 취소");

    private final String description;

    public static EventStatus fromDescription(String description) {
        for (EventStatus eventStatus : EventStatus.values()) {
            if (eventStatus.description.equals(description)) {
                return eventStatus;
            }
        }
        throw new IllegalArgumentException("Unknown InvoiceCorrectReason description: " + description);
    }
}
