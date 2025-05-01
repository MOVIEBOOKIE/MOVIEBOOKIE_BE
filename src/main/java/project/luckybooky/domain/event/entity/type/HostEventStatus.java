package project.luckybooky.domain.event.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum HostEventStatus {
    RECRUITING("모집 중"),
    RECRUIT_CANCELLED("모집 취소"),
    SCREENING_NOT_POSSIBLE("상영 진행 불가"),
    VENUE_SELECTION_PENDING("대관 여부 선택"),
    VENUE_BOOKING_PENDING("대관 대기"),
    NOTIFICATION_RESPONSE_CONFIRMED("알림 회신 확인"),
    SCREENING_COMPLETED("상영 완료"),
    NOT_SCREENED("상영 안함");

    private final String description;

    public static HostEventStatus fromDescription(String description) {
        for (HostEventStatus hostEventStatus : HostEventStatus.values()) {
            if (hostEventStatus.description.equals(description)) {
                return hostEventStatus;
            }
        }
        throw new IllegalArgumentException("Unknown InvoiceCorrectReason description: " + description);
    }
}
