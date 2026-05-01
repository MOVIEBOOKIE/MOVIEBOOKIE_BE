package project.luckybooky.domain.admin.batch;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.global.messaging.service.NotificationOutboxProducer;

public class AdminGlobalNotificationItemWriter implements ItemWriter<User>, StepExecutionListener {

    public static final String CONTEXT_PUSH_SENT_COUNT = "pushSentCount";
    public static final String CONTEXT_PUSH_SKIPPED_COUNT = "pushSkippedCount";
    public static final String CONTEXT_SAVED_COUNT = "savedCount";
    public static final String CONTEXT_PROCESSED_COUNT = "processedCount";
    public static final String CONTEXT_BATCH_COUNT = "batchCount";

    private final String title;
    private final String body;
    private final NotificationOutboxProducer notificationOutboxProducer;

    private long pushSentCount;
    private long pushSkippedCount;
    private long savedCount;
    private long processedCount;
    private int batchCount;
    private StepExecution stepExecution;

    public AdminGlobalNotificationItemWriter(
            String title,
            String body,
            NotificationOutboxProducer notificationOutboxProducer
    ) {
        this.title = title;
        this.body = body;
        this.notificationOutboxProducer = notificationOutboxProducer;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    @Override
    public void write(Chunk<? extends User> chunk) {
        batchCount++;
        for (User user : chunk.getItems()) {
            notificationOutboxProducer.enqueueDirectNotification(user.getId(), title, body, null);
            savedCount++;

            if (user.getFcmToken() == null || user.getFcmToken().isBlank()) {
                pushSkippedCount++;
            } else {
                pushSentCount++;
            }
            processedCount++;
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        this.stepExecution.getJobExecution().getExecutionContext()
                .putLong(CONTEXT_PUSH_SENT_COUNT, pushSentCount);
        this.stepExecution.getJobExecution().getExecutionContext()
                .putLong(CONTEXT_PUSH_SKIPPED_COUNT, pushSkippedCount);
        this.stepExecution.getJobExecution().getExecutionContext()
                .putLong(CONTEXT_SAVED_COUNT, savedCount);
        this.stepExecution.getJobExecution().getExecutionContext()
                .putLong(CONTEXT_PROCESSED_COUNT, processedCount);
        this.stepExecution.getJobExecution().getExecutionContext()
                .putInt(CONTEXT_BATCH_COUNT, batchCount);
        return ExitStatus.COMPLETED;
    }
}
