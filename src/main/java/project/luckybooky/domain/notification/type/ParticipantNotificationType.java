package project.luckybooky.domain.notification.type;

import lombok.Getter;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@Getter
public enum ParticipantNotificationType {
    APPLY_COMPLETED("10", "이벤트 신청 완료",
            "\"%s\"\n이벤트 신청이 완료됐어요!\n멋진 만남을 기다려볼까요🙌?"),
    APPLY_CANCEL("11", "이벤트 신청 취소",
            "\"%s\"\n이벤트 신청이 취소됐어요.\n아쉽지만, 다음에 꼭 함께해요!"),
    EVENT_DELETED("12", "이벤트 삭제",
            "\"%s\"\n이벤트가 취소됐어요. 아쉽지만,\n다음에 꼭 함께해요!"),
    RECRUITMENT_CANCELLED("13", "이벤트 모집 마감",
            "\"%s\"\n이벤트가 인원 부족으로 취소됐어요.\n아쉽지만, 다음에 꼭 함께해요!"),
    RECRUITMENT_COMPLETED("14", "이벤트 모집 완료",
            "\"%s\"\n이벤트 모집이 완료됐어요!\n주최자가 대관신청중이에요"),
    RESERVATION_NOT_APPLIED("15", "이벤트 대관 취소",
            "\"%s\"\n이벤트 대관이 취소됐어요.\n아쉽지만, 다음에 꼭 함께해요!"),
    RESERVATION_CONFIRMED("16", "이벤트 대관 확정",
            "\"%s\"\n이벤트 대관이 확정됐어요.\n주최자의 연락을 꼭 확인해 주세요!"),
    SCREENING_COMPLETED("17", "이벤트 상영 완료 후기 요청",
            "\"%s\"\n이벤트가 잘 마무리됐나요?\n함께한 시간의 후기를 남겨주세요 🙂");

    private final String code;
    private final String title;
    private final String template;

    ParticipantNotificationType(String code, String title, String template) {
        this.code = code;
        this.title = title;
        this.template = template;
    }

    public String formatBody(String eventName) {
        return String.format(template, eventName);
    }

    public static ParticipantNotificationType fromCode(String code) {
        for (ParticipantNotificationType t : values()) {
            if (t.code.equals(code)) {
                return t;
            }
        }
        throw new BusinessException(ErrorCode.NOTIFICATION_TYPE_NOT_FOUND);
    }

}
