package project.luckybooky.domain.notification.job;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;
import project.luckybooky.domain.notification.repository.NotificationRepository;
import project.luckybooky.global.lock.DistributedEventLockService;

@Component
@Slf4j
@RequiredArgsConstructor
public class DeleteNotificationJob implements Job {
    private static final String DELETE_NOTIFICATION_LOCK_KEY = "scheduler:notification:delete";

    private final NotificationRepository notificationRepository;
    private final DistributedEventLockService distributedEventLockService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        RLock lock = distributedEventLockService.tryLock(DELETE_NOTIFICATION_LOCK_KEY, 0, 180_000);
        if (lock == null) {
            log.info("Skip DeleteNotificationJob: lock is already held by another instance");
            return;
        }

        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        try {
            notificationRepository.deleteOlderThan(cutoff);
        } finally {
            distributedEventLockService.unlockSafely(lock);
        }
    }

}
