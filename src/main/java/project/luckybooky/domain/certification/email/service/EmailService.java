package project.luckybooky.domain.certification.email.service;

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
    private final MeterRegistry meterRegistry;
    private final SecureRandom random = new SecureRandom();

    private final Map<String, CacheEntry> codeCache = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry> lockCache = new ConcurrentHashMap<>();
    private ScheduledExecutorService cleaner;

    public EmailService(EmailCertificationUtil mailUtil,
                        @Qualifier("mailExecutor") TaskExecutor mailExecutor,
                        UserRepository userRepository,
                        MeterRegistry meterRegistry) {
        this.mailUtil = mailUtil;
        this.mailExecutor = mailExecutor;
        this.userRepository = userRepository;
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    private void init() {
        cleaner = Executors.newSingleThreadScheduledExecutor();
        cleaner.scheduleAtFixedRate(this::cleanUp, 30, 30, TimeUnit.SECONDS);
    }

    @PreDestroy
    private void destroy() {
        cleaner.shutdownNow();
    }

    /**
     * 인증번호 발송 (비동기 처리)
     **/
    public void sendCode(EmailRequestDTO dto) {
        String email = dto.getEmail();
        String lockKey = PREFIX + email + ":lock";
        String codeKey = PREFIX + email;

        try {
            // 이메일 형식 검증
            validateEmailFormat(email);

            // 중복 발송 방지
            if (!acquireLock(lockKey, LOCK_TTL)) {
                throw new BusinessException(ErrorCode.CERTIFICATION_DUPLICATED);
            }

            codeCache.remove(codeKey);
            String code = generate();
            codeCache.put(codeKey, new CacheEntry(code, Instant.now().plus(CODE_TTL)));

            // 큐잉 시간 측정
            Timer.Sample enqueueSample = Timer.start(meterRegistry);
            mailExecutor.execute(() -> {
                // 전송 실행 시간 측정
                Timer.Sample execSample = Timer.start(meterRegistry);
                try {
                    mailUtil.sendMail(email, code);
                    log.info("이메일 인증번호 발송 성공: {}", email);
                } catch (BusinessException e) {
                    throw e;
                } catch (Exception e) {
                    log.error("이메일 전송 중 예상치 못한 오류: email={}, error={}", email, e.getMessage(), e);
                    throw new BusinessException(ErrorCode.EMAIL_SEND_FAIL);
                } finally {
                    execSample.stop(meterRegistry.timer("email.send.execution"));
                }
            });
            enqueueSample.stop(meterRegistry.timer("email.send.enqueue"));

            log.info("이메일 인증번호 발송 요청 성공: {}", email);

        } catch (BusinessException e) {
            log.warn("이메일 인증번호 발송 실패: email={}, error={}", email, e.getErrorCode());
            throw e;
        } catch (Exception e) {
            log.error("이메일 인증번호 발송 중 예상치 못한 오류: email={}, error={}", email, e.getMessage(), e);
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAIL);
        }
    }

    /**
     * 인증번호 검증
     **/
    @Transactional
    public void verify(EmailVerifyRequestDTO dto, String loginEmail) {
        String email = dto.getEmail();
        String code = dto.getCertificationCode();
        String codeKey = PREFIX + email;

        try {
            // 입력값 검증
            validateEmailFormat(email);
            validateCertificationCode(code);

            CacheEntry entry = codeCache.get(codeKey);
            if (entry == null || entry.isExpired()) {
                codeCache.remove(codeKey);
                throw new BusinessException(ErrorCode.CERTIFICATION_EXPIRED);
            }
            if (!entry.value.equals(code)) {
                throw new BusinessException(ErrorCode.CERTIFICATION_MISMATCH);
            }

            User user = userRepository.findByEmail(loginEmail)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            // 이미 인증된 이메일인지 확인
            if (email.equals(user.getCertificationEmail())) {
                throw new BusinessException(ErrorCode.CERTIFICATION_ALREADY_VERIFIED);
            }

            user.setCertificationEmail(email);
            codeCache.remove(codeKey);
            log.info("이메일 인증번호 검증 성공: email={}, user={}", email, loginEmail);

        } catch (BusinessException e) {
            log.warn("이메일 인증번호 검증 실패: email={}, error={}", email, e.getErrorCode());
            throw e;
        } catch (Exception e) {
            log.error("이메일 인증번호 검증 중 예상치 못한 오류: email={}, error={}", email, e.getMessage(), e);
            throw new BusinessException(ErrorCode.CERTIFICATION_MISMATCH);
        }
    }

    private boolean acquireLock(String key, Duration ttl) {
        Instant now = Instant.now();
        AtomicBoolean acquired = new AtomicBoolean(false);
        lockCache.compute(key, (k, existing) -> {
            if (existing == null || existing.isExpiredAt(now)) {
                acquired.set(true);
                return new CacheEntry("1", now.plus(ttl));
            }
            return existing;
        });
        return acquired.get();
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

    private void validateEmailFormat(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.EMAIL_INVALID_FORMAT);
        }

        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            log.error("잘못된 이메일 형식: {}", email);
            throw new BusinessException(ErrorCode.EMAIL_INVALID_FORMAT);
        }
    }

    private void validateCertificationCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.CERTIFICATION_INVALID_FORMAT);
        }

        if (!code.matches("^[0-9]{" + CODE_LEN + "}$")) {
            log.error("잘못된 인증번호 형식: {}", code);
            throw new BusinessException(ErrorCode.CERTIFICATION_INVALID_FORMAT);
        }
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
