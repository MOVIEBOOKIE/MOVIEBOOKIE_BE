package project.luckybooky.domain.certification.sms.Service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.transaction.Transactional;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import project.luckybooky.domain.certification.sms.dto.request.SmsRequestDTO;
import project.luckybooky.domain.certification.sms.dto.request.SmsVerifyRequestDTO;
import project.luckybooky.domain.certification.sms.util.SmsCertificationUtil;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.repository.UserRepository;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;
import project.luckybooky.global.redis.SmsCertificationCache;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    private static final int CODE_LEN = 4;
    private static final Duration CODE_TTL = Duration.ofMinutes(3);
    private static final Duration LOCK_TTL = Duration.ofSeconds(10);
    private static final String PREFIX = "otp:sms:";

    private final SmsCertificationUtil smsUtil;
    private final SmsCertificationCache cache;
    private final UserRepository userRepository;
    private final TaskExecutor smsExecutor;
    private final MeterRegistry meterRegistry;

    private final SecureRandom random = new SecureRandom();
    private final Map<String, Instant> lockMap = new ConcurrentHashMap<>();
    private ScheduledExecutorService cleaner;

    // 캐시 락 정리 스케줄러
    @PostConstruct
    private void init() {
        cleaner = Executors.newSingleThreadScheduledExecutor();
        cleaner.scheduleAtFixedRate(this::cleanUpLocks, 30, 30, TimeUnit.SECONDS);
    }

    @PreDestroy
    private void destroy() {
        cleaner.shutdownNow();
    }

    /**
     * 1) 인증번호 발송
     **/
    public void sendCertificationCode(SmsRequestDTO dto) {
        String phone = dto.getPhoneNum();
        String lockKey = PREFIX + phone + ":lock";

        if (!acquireLock(lockKey, LOCK_TTL)) {
            throw new BusinessException(ErrorCode.CERTIFICATION_DUPLICATED);
        }

        // 코드 생성 및 저장
        String code = generateCode();
        cache.remove(PREFIX + phone);
        cache.store(PREFIX + phone, code, CODE_TTL);

        Timer.Sample enqueue = Timer.start(meterRegistry);
        smsExecutor.execute(() -> {
            Timer.Sample exec = Timer.start(meterRegistry);
            try {
                smsUtil.sendSMS(phone, code);
            } catch (Exception e) {
                log.error("Failed to send SMS to {}: {}", phone, e.getMessage(), e);
            } finally {
                exec.stop(meterRegistry.timer("sms.send.execution"));
            }
        });
        enqueue.stop(meterRegistry.timer("sms.send.enqueue"));

        log.debug("Queued SMS send for {}", phone);
    }

    /**
     * 2) 인증번호 검증 & 전화번호 저장
     **/
    @Transactional
    public void verifyCertificationCode(SmsVerifyRequestDTO dto, String loginEmail) {
        String phone = dto.getPhoneNum();
        String key = PREFIX + phone;
        String saved = cache.get(key);

        if (saved == null) {
            throw new BusinessException(ErrorCode.CERTIFICATION_EXPIRED);
        }
        if (!saved.equals(dto.getCertificationCode())) {
            throw new BusinessException(ErrorCode.CERTIFICATION_MISMATCH);
        }

        User user = userRepository.findByEmail(loginEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        userRepository.findByPhoneNumber(phone)
                .filter(other -> !other.getId().equals(user.getId()))
                .ifPresent(any -> {
                    throw new BusinessException(ErrorCode.PHONE_ALREADY_USED);
                });

        user.setPhoneNumber(phone);
        cache.remove(key);
        log.debug("✅ {} verified & saved to user {}", phone, loginEmail);
    }

    private boolean acquireLock(String key, Duration ttl) {
        Instant now = Instant.now();
        AtomicBoolean acquired = new AtomicBoolean(false);
        lockMap.compute(key, (k, expireAt) -> {
            if (expireAt == null || now.isAfter(expireAt)) {
                acquired.set(true);
                return now.plus(ttl);
            }
            return expireAt;
        });
        return acquired.get();
    }

    private void cleanUpLocks() {
        Instant now = Instant.now();
        lockMap.entrySet().removeIf(e -> now.isAfter(e.getValue()));
    }

    private String generateCode() {
        int bound = (int) Math.pow(10, CODE_LEN);
        return String.format("%0" + CODE_LEN + "d", random.nextInt(bound));
    }
}
