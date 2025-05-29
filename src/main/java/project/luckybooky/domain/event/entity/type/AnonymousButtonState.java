package project.luckybooky.domain.event.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AnonymousButtonState {
    REGISTER("신청하기"),
    RECRUIT_DONE("모집 완료"),
    ;

    private final String description;

    public static AnonymousButtonState fromDescription(String description) {
        for (AnonymousButtonState anonymousButtonState : AnonymousButtonState.values()) {
            if (anonymousButtonState.description.equals(description)) {
                return anonymousButtonState;
            }
        }
        throw new IllegalArgumentException("Unknown InvoiceCorrectReason description: " + description);
    }
}
