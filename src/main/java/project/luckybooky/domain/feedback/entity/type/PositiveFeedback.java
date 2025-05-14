package project.luckybooky.domain.feedback.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PositiveFeedback {
    NONE("없음"),
    RECOMMEND_PERSONALIZED_EVENT("내게 꼭 맞는 이벤트를 추천해줘요"),
    SIMPLE_EVENT_REGISTRATION("이벤트 신청/모집 과정이 간단해요"),
    DETAILED_EVENT_INFO_AVAILABLE("이벤트 정보를 자세히 확인할 수 있어요"),
    NOTIFICATION_TRACKING_HELPFUL("알림으로 진행 과정을 확인할 수 있어 좋아요"),
    SATISFIED_WITH_LOCATION("대관한 영화관이 마음에 들어요"),
    UNFORGETTABLE_EXPERIENCE("덕분에 잊지 못할 추억을 만들 수 있었어요"),
    ;

    private final String description;

    public static PositiveFeedback fromDescription(String description) {
        for (PositiveFeedback positiveFeedback : PositiveFeedback.values()) {
            if (positiveFeedback.description.equals(description)) {
                return positiveFeedback;
            }
        }
        throw new IllegalArgumentException("Unknown InvoiceCorrectReason description: " + description);
    }
    }
