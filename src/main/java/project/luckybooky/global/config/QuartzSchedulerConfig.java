package project.luckybooky.global.config;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import project.luckybooky.domain.notification.job.DeleteNotificationJob;

@Configuration
public class QuartzSchedulerConfig {

    private static final String JOB_ID = "deleteNotificationJob";
    private static final String TRIGGER_ID = "deleteNotificationTrigger";

    @Bean
    public JobDetail purgeJobDetail() {
        return JobBuilder.newJob(DeleteNotificationJob.class)
                .withIdentity(JOB_ID)
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger purgeJobTrigger(JobDetail purgeJobDetail) {
        CronScheduleBuilder schedule = CronScheduleBuilder.cronSchedule("0 0 0 * * ?");
        return TriggerBuilder.newTrigger()
                .forJob(purgeJobDetail)
                .withIdentity(TRIGGER_ID)
                .withSchedule(schedule)
                .build();
    }

}
