package project.luckybooky.domain.notification.type;

import lombok.Getter;

@Getter
public enum HostNotificationType {
    EVENT_CREATED("0.1", "이벤트 생성 완료 알림",
            "\"%s\"\n이벤트 생성이 완료됐어요! 모집 마감까지 함께 기다려요☺"),
    EVENT_DELETED("0.2", "이벤트 삭제 알림",
            "\"%s\"\n이벤트 삭제가 완료됐어요. 아쉽지만, 다음에 꼭 함께해요!"),
    RECRUITMENT_CANCELLED("0.3", "모집 마감 알림 (인원부족)",
            "\"%s\"\n이벤트가 인원 부족으로 취소됐어요. 아쉽지만, 다음에 꼭 함께해요!"),
    RECRUITMENT_COMPLETED("0.4", "모집 마감 알림 (인원충족)",
            "\"%s\"\n이벤트 모집이 완료됐어요! 대관 신청하러 가볼까요?"),
    RESERVATION_CONFIRMED("0.5", "대관 확정 알림",
            "\"%s\"\n이벤트 대관이 확정됐어요. 무비부키 메일을 꼭 확인해 주세요!"),
    RESERVATION_DENIED("0.6", "대관 불가 알림",
            "\"%s\"\n이벤트 대관이 승인되지 않았어요. 무비부키 메일을 확인해주세요!"),
    SCREENING_COMPLETED("0.7", "상영 완료 후기 요청 알림",
            "\"%s\"\n이벤트가 잘 마무리됐나요? 함께한 시간의 후기를 남겨주세요 🙂");

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
}