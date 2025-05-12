package project.luckybooky.domain.event.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventConstants {
    REGISTER_SUCCESS("이벤트 신청이 정상적으로 완료되었습니다."),
    REGISTER_CANCEL_SUCCESS("이벤트 신청 취소가 정상적으로 완료되었습니다."),
    ;

    private final String message;
}
