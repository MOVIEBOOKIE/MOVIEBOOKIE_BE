package project.luckybooky.domain.admin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminBulkNotificationResponse {
    private final Long eventId;
    private final AdminBulkNotificationTargetType targetType;
    private final Integer targetCount;
    private final Integer pushSentCount;
    private final Integer pushSkippedCount;
    private final Integer savedCount;
}
