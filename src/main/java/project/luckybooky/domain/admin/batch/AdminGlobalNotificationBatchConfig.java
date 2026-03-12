package project.luckybooky.domain.admin.batch;

import java.util.HashMap;
import java.util.Map;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import project.luckybooky.domain.notification.repository.NotificationRepository;
import project.luckybooky.domain.notification.service.NotificationService;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.repository.UserRepository;

@Configuration
public class AdminGlobalNotificationBatchConfig {

    @Bean
    public Job adminGlobalNotificationJob(
            JobRepository jobRepository,
            @Qualifier("adminGlobalNotificationStep") Step adminGlobalNotificationStep
    ) {
        return new JobBuilder("adminGlobalNotificationJob", jobRepository)
                .start(adminGlobalNotificationStep)
                .build();
    }

    @Bean
    @JobScope
    public Step adminGlobalNotificationStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            RepositoryItemReader<User> adminGlobalNotificationUserReader,
            AdminGlobalNotificationItemWriter adminGlobalNotificationItemWriter,
            @Value("#{jobParameters['batchSize']}") Long batchSize
    ) {
        int chunkSize = batchSize == null ? 500 : Math.toIntExact(batchSize);
        return new StepBuilder("adminGlobalNotificationStep", jobRepository)
                .<User, User>chunk(chunkSize, transactionManager)
                .reader(adminGlobalNotificationUserReader)
                .writer(adminGlobalNotificationItemWriter)
                .listener(adminGlobalNotificationItemWriter)
                .build();
    }

    @Bean
    @StepScope
    public RepositoryItemReader<User> adminGlobalNotificationUserReader(
            UserRepository userRepository,
            @Value("#{jobParameters['batchSize']}") Long batchSize
    ) {
        int pageSize = batchSize == null ? 500 : Math.toIntExact(batchSize);
        RepositoryItemReader<User> reader = new RepositoryItemReader<>();
        reader.setRepository(userRepository);
        reader.setMethodName("findAll");
        reader.setPageSize(pageSize);
        reader.setSaveState(false);

        Map<String, Sort.Direction> sorts = new HashMap<>();
        sorts.put("id", Sort.Direction.ASC);
        reader.setSort(sorts);
        return reader;
    }

    @Bean
    @StepScope
    public AdminGlobalNotificationItemWriter adminGlobalNotificationItemWriter(
            NotificationService notificationService,
            NotificationRepository notificationRepository,
            @Value("#{jobParameters['title']}") String title,
            @Value("#{jobParameters['body']}") String body
    ) {
        return new AdminGlobalNotificationItemWriter(
                title,
                body,
                notificationService,
                notificationRepository
        );
    }
}
