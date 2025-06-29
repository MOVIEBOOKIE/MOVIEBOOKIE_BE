package project.luckybooky.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean("mailExecutor")
    public TaskExecutor mailExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(50);
        exec.setMaxPoolSize(200);
        exec.setQueueCapacity(500);
        exec.setThreadNamePrefix("mail-exec-");
        exec.initialize();
        return exec;
    }

    @Bean("smsExecutor")
    public TaskExecutor smsExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(20);
        exec.setMaxPoolSize(50);
        exec.setQueueCapacity(200);
        exec.setThreadNamePrefix("sms-exec-");
        exec.initialize();
        return exec;
    }
}

