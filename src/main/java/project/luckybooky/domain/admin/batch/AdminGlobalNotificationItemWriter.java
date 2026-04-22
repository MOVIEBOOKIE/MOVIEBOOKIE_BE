package project.luckybooky.domain.admin.batch;

import com.google.firebase.messaging.Message;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import project.luckybooky.domain.notification.converter.NotificationConverter;
import project.luckybooky.domain.notification.entity.NotificationInfo;
import project.luckybooky.domain.notification.repository.NotificationRepository;
import project.luckybooky.domain.notification.service.NotificationService;
import project.luckybooky.domain.user.entity.User;

public class AdminGlobalNotificationItemWriter implements ItemWriter<User>, StepExecutionListener {

    public static final String CONTEXT_PUSH_SENT_COUNT = "pushSentCount";
    public static final String CONTEXT_PUSH_SKIPPED_COUNT = "pushSkippedCount";
    public static final String CONTEXT_SAVED_COUNT = "savedCount";
    public static final String CONTEXT_PROCESSED_COUNT = "processedCount";
    public static final String CONTEXT_BATCH_COUNT = "batchCount";

    private final String title;
    private final String body;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    private long pushSentCount;
    private long pushSkippedCount;
    private long savedCount;
    private long processedCount;
    private int batchCount;
    private StepExecution stepExecution;

    public AdminGlobalNotificationItemWriter(
            String title,
            String body,
            NotificationService notificationService,
            NotificationRepository notificationRepository
    ) {
        this.title = title;
        this.body = body;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    @Override
    public void write(Chunk<? extends User> chunk) {
        batchCount++;
        List<NotificationInfo> infos = new ArrayList<>(chunk.size());

        for (User user : chunk.getItems()) {
            Message message = NotificationConverter.toMessage(user, title, body);
            if (message == null) {
                pushSkippedCount++;
            } else {
                notificationService.send(message);
                pushSentCount++;
            }

            infos.add(NotificationInfo.builder()
                    .user(user)
                    .title(title)
                    .body(body)
                    .eventId(null)
                    .sentAt(LocalDateTime.now())
                    .isRead(false)
                    .build());
            processedCount++;
        }

        if (!infos.isEmpty()) {
            notificationRepository.saveAll(infos);
            savedCount += infos.size();
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
