package project.luckybooky.domain.feedback.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NegativeFeedback {
    EVENT_SEARCH_INCONVENIENT("원하는 이벤트를 찾기 불편해요"),
    EVENT_APPLICATION_HARD("이벤트 신청/모집 과정이 어려워요"),
    TOO_LONG_TO_SCREENING("이벤트 상영까지 진행 과정이 너무 길어요"),
    NOTIFICATION_CHECK_INCONVENIENT("알림으로 진행 상황을 확인하는 게 불편해요"),
    LIMITED_AVAILABLE_LOCATIONS("대관 가능한 영화관이 너무 한정적이에요"),
    EVENT_CANCELLED_DISAPPOINTED("이벤트가 취소되어 실망스러워요"),
    ;

    private final String description;

    public static NegativeFeedback fromDescription(String description) {
        for (NegativeFeedback negativeFeedback : NegativeFeedback.values()) {
            if (negativeFeedback.description.equals(description)) {
                return negativeFeedback;
            }
        }
        throw new IllegalArgumentException("Unknown InvoiceCorrectReason description: " + description);
    }
}
