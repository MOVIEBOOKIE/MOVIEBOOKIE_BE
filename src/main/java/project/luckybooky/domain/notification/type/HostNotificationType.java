package project.luckybooky.domain.notification.type;

import lombok.Getter;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@Getter
public enum HostNotificationType {
    EVENT_CREATED("1", "이벤트 생성 완료",
            "\"%s\"\n이벤트 생성이 완료됐어요!\n모집 마감까지 함께 기다려요☺"),
    EVENT_DELETED("2", "이벤트 삭제",
            "\"%s\"\n이벤트 삭제가 완료됐어요.\n아쉽지만, 다음에 꼭 함께해요!"),
    RECRUITMENT_CANCELLED("3", "이벤트 모집 마감",
            "\"%s\"\n이벤트가 인원 부족으로 취소됐어요.\n아쉽지만, 다음에 꼭 함께해요!"),
    RECRUITMENT_COMPLETED("4", "이벤트 모집 마감",
            "\"%s\"\n이벤트 모집이 완료됐어요!\n대관 신청하러 가볼까요?"),
    RESERVATION_CONFIRMED("5", "이벤트 대관 확정",
            "\"%s\"\n이벤트 대관이 확정됐어요.\n무비부키 메일을 꼭 확인해 주세요!"),
    RESERVATION_DENIED("6", "이벤트 대관 불가",
            "\"%s\"\n이벤트 대관이 승인되지 않았어요.\n무비부키 메일을 확인해주세요!"),
    SCREENING_COMPLETED("7", "이벤트 상영 완료",
            "\"%s\"\n이벤트가 잘 마무리됐나요?\n함께한 시간의 후기를 남겨주세요 🙂");

    private final String code;
    private final String title;
    private final String template;

    HostNotificationType(String code, String title, String template) {
        this.code = code;
        this.title = title;
        this.template = template;
    }

    public String formatBody(String eventName) {
        return String.format(template, eventName);
    }

    public static HostNotificationType fromCode(String code) {
        for (HostNotificationType t : values()) {
            if (t.code.equals(code)) {
                return t;
            }
        }
        throw new BusinessException(ErrorCode.NOTIFICATION_TYPE_NOT_FOUND);
    }
}