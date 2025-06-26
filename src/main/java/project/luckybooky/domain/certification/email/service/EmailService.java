package project.luckybooky.domain.certification.email.service;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import project.luckybooky.domain.certification.email.dto.request.EmailRequestDTO;
import project.luckybooky.domain.certification.email.dto.request.EmailVerifyRequestDTO;
import project.luckybooky.domain.certification.email.util.EmailCertificationUtil;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.repository.UserRepository;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@Service
@Slf4j
public class EmailService {

    private static final int CODE_LEN = 4;
    private static final Duration CODE_TTL = Duration.ofMinutes(3);
    private static final Duration LOCK_TTL = Duration.ofSeconds(10);
    private static final String PREFIX = "otp:email:";

    private final EmailCertificationUtil mailUtil;
    private final TaskExecutor mailExecutor;
    private final UserRepository userRepository;
    private final SecureRandom random = new SecureRandom();

    private final Map<String, CacheEntry> codeCache = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry> lockCache = new ConcurrentHashMap<>();
    private ScheduledExecutorService cleaner;

    public EmailService(EmailCertificationUtil mailUtil,
                        @Qualifier("mailExecutor") TaskExecutor mailExecutor,
                        UserRepository userRepository) {
        this.mailUtil = mailUtil;
        this.mailExecutor = mailExecutor;
        this.userRepository = userRepository;
    }

    @PostConstruct
    private void init() {
        cleaner = Executors.newSingleThreadScheduledExecutor();
        // 30초마다 만료된 엔트리 정리
        cleaner.scheduleAtFixedRate(this::cleanUp, 30, 30, TimeUnit.SECONDS);
    }

    @PreDestroy
    private void destroy() {
        cleaner.shutdownNow();
    }

    /**
     * 인증번호 발송
     **/
    public void sendCode(EmailRequestDTO dto) {
        String email = dto.getEmail();
        String lockKey = PREFIX + email + ":lock";
        String codeKey = PREFIX + email;

        if (!acquireLock(lockKey, LOCK_TTL)) {
            throw new BusinessException(ErrorCode.CERTIFICATION_DUPLICATED);
        }

        codeCache.remove(codeKey);
        String code = generate();
        codeCache.put(codeKey, new CacheEntry(code, Instant.now().plus(CODE_TTL)));

        // 메일 전송 비동기 처리
        mailExecutor.execute(() -> {
            try {
                mailUtil.sendMail(email, code);
                log.info("Asynchronously sent email code {} → {}", code, email);
            } catch (Exception e) {
                log.error("Failed to send mail to {}: {}", email, e.getMessage(), e);
            }
        });

        log.info("Queued mail send for {}", email);
    }

    /**
     * 인증번호 검증
     **/
    @Transactional
    public void verify(EmailVerifyRequestDTO dto, String loginEmail) {
        String codeKey = PREFIX + dto.getEmail();
        CacheEntry entry = codeCache.get(codeKey);
        if (entry == null || entry.isExpired()) {
            codeCache.remove(codeKey);
            throw new BusinessException(ErrorCode.CERTIFICATION_EXPIRED);
        }
        if (!entry.value.equals(dto.getCertificationCode())) {
            throw new BusinessException(ErrorCode.CERTIFICATION_MISMATCH);
        }

        User user = userRepository.findByEmail(loginEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.setCertificationEmail(dto.getEmail());
        codeCache.remove(codeKey);
        log.debug("✅ {} verified & saved to user {}", dto.getEmail(), loginEmail);
    }

    private boolean acquireLock(String key, Duration ttl) {
        Instant now = Instant.now();
        lockCache.compute(key, (k, existing) -> {
            if (existing == null || existing.isExpiredAt(now)) {
                return new CacheEntry("1", now.plus(ttl));
            }
            return existing;
        });
        CacheEntry post = lockCache.get(key);
        return post.expireAt.isAfter(now);
    }

    private void cleanUp() {
        Instant now = Instant.now();
        codeCache.entrySet().removeIf(e -> e.getValue().isExpiredAt(now));
        lockCache.entrySet().removeIf(e -> e.getValue().isExpiredAt(now));
    }

    private String generate() {
        int bound = (int) Math.pow(10, CODE_LEN);
        return String.format("%0" + CODE_LEN + "d", random.nextInt(bound));
    }

    private static class CacheEntry {
        final String value;
        final Instant expireAt;

        CacheEntry(String value, Instant expireAt) {
            this.value = value;
            this.expireAt = expireAt;
        }

        boolean isExpired() {
            return isExpiredAt(Instant.now());
        }

        boolean isExpiredAt(Instant now) {
            return now.isAfter(expireAt);
        }
    }
}