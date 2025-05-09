package project.luckybooky.domain.event.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum EventStatus {
    RECRUITING("모집 중"),
    RECRUITED("모집 완료"),
    VENUE_PENDING("대관 검토 중"),
    VENUE_CONFIRMED("대관 확정"),
    CANCELLED("상영 취소"),
    COMPLETED("상영 완료");

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
