package project.luckybooky.domain.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.luckybooky.domain.admin.batch.AdminGlobalNotificationItemWriter;
import project.luckybooky.domain.admin.dto.AdminGlobalNotificationRequest;
import project.luckybooky.domain.admin.dto.AdminGlobalNotificationResponse;
import project.luckybooky.domain.user.repository.UserRepository;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;
import project.luckybooky.global.messaging.service.OutboxReplayService;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminGlobalNotificationService {

    private static final int DEFAULT_BATCH_SIZE = 500;

    private final UserRepository userRepository;
    private final OutboxReplayService outboxReplayService;
    private final JobLauncher jobLauncher;
    @Qualifier("adminGlobalNotificationJob")
    private final Job adminGlobalNotificationJob;

    public AdminGlobalNotificationResponse sendToAllUsers(AdminGlobalNotificationRequest request) {
        int effectiveBatchSize = request.getBatchSize() == null ? DEFAULT_BATCH_SIZE : request.getBatchSize();
        long totalTargetCount = userRepository.count();
        JobExecution jobExecution = runGlobalNotificationJob(request, effectiveBatchSize);
        if (jobExecution.getStatus() != BatchStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.ADMIN_BATCH_EXECUTION_FAILED);
        }

        return AdminGlobalNotificationResponse.builder()
                .requestedBatchSize(request.getBatchSize())
                .effectiveBatchSize(effectiveBatchSize)
                .totalTargetCount(totalTargetCount)
                .processedCount(getContextLong(jobExecution, AdminGlobalNotificationItemWriter.CONTEXT_PROCESSED_COUNT))
                .pushSentCount(getContextLong(jobExecution, AdminGlobalNotificationItemWriter.CONTEXT_PUSH_SENT_COUNT))
                .pushSkippedCount(getContextLong(jobExecution, AdminGlobalNotificationItemWriter.CONTEXT_PUSH_SKIPPED_COUNT))
                .queuedCount(getContextLong(jobExecution, AdminGlobalNotificationItemWriter.CONTEXT_QUEUED_COUNT))
                .savedCount(getContextLong(jobExecution, AdminGlobalNotificationItemWriter.CONTEXT_QUEUED_COUNT))
                .totalBatches(getContextInt(jobExecution, AdminGlobalNotificationItemWriter.CONTEXT_BATCH_COUNT))
                .build();
    }

    private JobExecution runGlobalNotificationJob(AdminGlobalNotificationRequest request, int effectiveBatchSize) {
        String requestId = java.util.UUID.randomUUID().toString();
        JobParameters parameters = new JobParametersBuilder()
                .addString("title", request.getTitle())
                .addString("body", request.getBody())
                .addLong("batchSize", (long) effectiveBatchSize)
                .addLong("requestedAt", System.currentTimeMillis())
                .addString("requestId", requestId)
                .toJobParameters();
        try {
            return jobLauncher.run(adminGlobalNotificationJob, parameters);
        } catch (Exception e) {
            log.error("Failed to launch adminGlobalNotificationJob. requestId={}, batchSize={}, title={}, reason={}",
                    requestId, effectiveBatchSize, request.getTitle(), e.getMessage(), e);
            throw new BusinessException(ErrorCode.ADMIN_BATCH_EXECUTION_FAILED);
        }
    }

    private long getContextLong(JobExecution jobExecution, String key) {
        return jobExecution.getExecutionContext().containsKey(key)
                ? jobExecution.getExecutionContext().getLong(key)
                : 0L;
    }

    private int getContextInt(JobExecution jobExecution, String key) {
        return jobExecution.getExecutionContext().containsKey(key)
                ? jobExecution.getExecutionContext().getInt(key)
                : 0;
    }

    @Transactional
    public int replayFailedOutbox(int limit) {
        return outboxReplayService.replayFailed(limit);
    }
}
