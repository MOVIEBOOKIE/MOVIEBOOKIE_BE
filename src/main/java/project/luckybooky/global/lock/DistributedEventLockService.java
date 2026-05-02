package project.luckybooky.global.lock;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedEventLockService {

    private final RedissonClient redissonClient;

    @Value("${app.lock.event-register.key-prefix:event:register:}")
    private String registerEventLockPrefix;

    @Value("${app.lock.event-register.wait-ms:0}")
    private long registerEventWaitMs;

    @Value("${app.lock.event-register.lease-ms:10000}")
    private long registerEventLeaseMs;

    public RLock tryLock(String lockKey, long waitMs, long leaseMs) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean acquired = lock.tryLock(waitMs, leaseMs, TimeUnit.MILLISECONDS);
            return acquired ? lock : null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.INVALID_OPERATION);
        }
    }

    public RLock tryLockForEventRegistration(Long eventId) {
        String lockKey = registerEventLockPrefix + eventId;
        return tryLock(lockKey, registerEventWaitMs, registerEventLeaseMs);
    }

    public void unlockSafely(RLock lock) {
        if (lock == null) {
            return;
        }
        try {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        } catch (Exception e) {
            log.warn("Failed to unlock distributed event lock: {}", e.getMessage());
        }
    }
}
