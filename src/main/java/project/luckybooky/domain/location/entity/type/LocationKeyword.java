package project.luckybooky.domain.location.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import project.luckybooky.domain.event.entity.type.EventStatus;

@RequiredArgsConstructor
@Getter
public enum LocationKeyword {
    LARGE_SCALE("대규모"),
    MEDIUM_SCALE("중규모"),
    SMALL_SCALE("소규모"),
    NORMAL("적당한"),
    PRIVATE("프라이빗한"),
    COZINESS("아늑한"),
    QUIETNESS("조용한"),
    LIVE_FEEL("현장감"),
    MAJESTIC("웅장감"),
    PREMIUM("프리미엄"),
    IMMERSION("몰입"),
    FOR_GROUP("단체"),
    FOR_COUPLE("연인"),
    FOR_FAMILY("가족");

    private final String description;

    public static LocationKeyword fromDescription(String description) {
        for (LocationKeyword locationKeyword : LocationKeyword.values()) {
            if (locationKeyword.description.equals(description)) {
                return locationKeyword;
            }
        }
        throw new IllegalArgumentException("Unknown InvoiceCorrectReason description: " + description);
    }
}
