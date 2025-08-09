package project.luckybooky.domain.notification.job;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;
import project.luckybooky.domain.notification.repository.NotificationRepository;

@Component
@Slf4j
@RequiredArgsConstructor
public class DeleteNotificationJob implements Job {

    private final NotificationRepository notificationRepository;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        notificationRepository.deleteOlderThan(cutoff);
    }

}
