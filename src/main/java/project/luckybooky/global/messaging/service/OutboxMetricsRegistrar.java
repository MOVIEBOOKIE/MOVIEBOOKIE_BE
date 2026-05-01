package project.luckybooky.global.messaging.service;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import project.luckybooky.global.messaging.entity.OutboxStatus;
import project.luckybooky.global.messaging.repository.OutboxEventRepository;

@Component
@RequiredArgsConstructor
public class OutboxMetricsRegistrar {

    private final MeterRegistry meterRegistry;
    private final OutboxEventRepository outboxEventRepository;

    @PostConstruct
    public void register() {
        Gauge.builder("notification.outbox.pending.count", outboxEventRepository, repo -> repo.countByStatus(OutboxStatus.PENDING))
                .register(meterRegistry);
        Gauge.builder("notification.outbox.failed.count", outboxEventRepository, repo -> repo.countByStatus(OutboxStatus.FAILED))
                .register(meterRegistry);
        Gauge.builder("notification.outbox.published.count", outboxEventRepository, repo -> repo.countByStatus(OutboxStatus.PUBLISHED))
                .register(meterRegistry);
    }
}
