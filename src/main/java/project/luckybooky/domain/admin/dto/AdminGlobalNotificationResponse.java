package project.luckybooky.domain.admin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminGlobalNotificationResponse {
    private final Integer requestedBatchSize;
    private final Integer effectiveBatchSize;
    private final Long totalTargetCount;
    private final Long processedCount;
    private final Long pushSentCount;
    private final Long pushSkippedCount;
    private final Long savedCount;
    private final Integer totalBatches;
}
