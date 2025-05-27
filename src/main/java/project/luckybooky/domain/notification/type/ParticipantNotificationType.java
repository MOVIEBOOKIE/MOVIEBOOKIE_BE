package project.luckybooky.domain.notification.type;

import lombok.Getter;

@Getter
public enum ParticipantNotificationType {
    APPLY_COMPLETED("0.2", "이벤트 신청 완료 알림", "%s 이벤트 신청이 완료됐어요! 멋진 만남을 기다려볼까요🙌?"),
    APPLY_CANCEL("0.3", "이벤트 신청 취소 알림", "%s 이벤트 신청이 취소됐어요. 아쉽지만, 다음에 꼭 함께해요!"),
    EVENT_DELETED("0.4", "이벤트 삭제 알림", "%s 이벤트가 취소됐어요. 아쉽지만, 다음에 꼭 함께해요!"),
    RECRUITMENT_CANCELLED("1.1", "모집 마감 알림 (인원부족)", "%s 이벤트가 인원 부족으로 취소됐어요. 아쉽지만, 다음에 꼭 함께해요!"),
    RECRUITMENT_COMPLETED("1.2", "모집 완료 알림", "%s 이벤트 모집이 완료됐어요! 주최자가 대관신청중이에요"),
    RESERVATION_NOT_APPLIED("2.2", "대관 취소 알림", "%s 이벤트 대관이 취소됐어요. 아쉽지만, 다음에 꼭 함께해요!"),
    RESERVATION_CONFIRMED("3.1", "대관 확정 알림", "%s 이벤트 대관이 확정됐어요. 주최자의 연락을 꼭 확인해 주세요!"),
    RESERVATION_DENIED("3.2", "대관 불가 알림", "%s 이벤트 대관이 승인되지 않았어요. 아쉽지만, 다음에 꼭 함께해요!"),
    SCREENING_COMPLETED("4", "상영 완료 후기 요청 알림", "%s 이벤트가 잘 마무리됐나요? 함께한 시간의 후기를 남겨주세요 🙂");

    private final String code;
    private final String title;
    private final String bodyTemplate;

    ParticipantNotificationType(String code, String title, String bodyTemplate) {
        this.code = code;
        this.title = title;
        this.bodyTemplate = bodyTemplate;
    }

    public String formatBody(String eventName) {
        return String.format(bodyTemplate, eventName);
    }
}
