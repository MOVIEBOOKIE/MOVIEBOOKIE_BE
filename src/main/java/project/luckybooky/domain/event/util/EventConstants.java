package project.luckybooky.domain.event.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventConstants {
    REGISTER_SUCCESS("이벤트 신청이 정상적으로 완료되었습니다."),
    REGISTER_CANCEL_SUCCESS("이벤트 신청 취소가 정상적으로 완료되었습니다."),
    RECRUIT_CANCEL_SUCCESS("이벤트 모집 취소가 정상적으로 완료되었습니다."),
    VENUE_RESERVATION_SUCCESS("대관 신청이 정상적으로 완료되었습니다."),
    VENUE_CANCEL_SUCCESS("대관 취소가 정상적으로 완료되었습니다."),
    VENUE_CONFIRMED("대관 확정이 정상적으로 완료되었습니다."),
    SCREENING_DONE_SUCCESS("상영 완료가 정상적으로 처리되었습니다."),
    SCREENING_CANCEL_SUCCESS("상영 취소가 정상적으로 처리되었습니다."),
    ;

    private final String message;
}
