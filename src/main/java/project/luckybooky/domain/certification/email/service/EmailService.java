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
    private final UserRepository userRepository;
    private final SecureRandom random = new SecureRandom();

    // 인 메모리 해쉬
    private final Map<String, CacheEntry> codeCache = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry> lockCache = new ConcurrentHashMap<>();

    // 만료된 캐시 자동 정리
    private ScheduledExecutorService cleaner;

    public EmailService(EmailCertificationUtil mailUtil,
                        UserRepository userRepository) {
        this.mailUtil = mailUtil;
        this.userRepository = userRepository;
    }

    @PostConstruct
    private void init() {
        cleaner = Executors.newSingleThreadScheduledExecutor();
        // 1분마다 만료된 엔트리 정리
        cleaner.scheduleAtFixedRate(this::cleanUp, 1, 1, TimeUnit.MINUTES);
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

        // 10초 동안 중복 요청 제한
        if (!acquireLock(lockKey, LOCK_TTL)) {
            throw new BusinessException(ErrorCode.CERTIFICATION_DUPLICATED);
        }

        // 기존 코드 제거
        codeCache.remove(codeKey);

        String code = generate();
        // 코드 저장
        codeCache.put(codeKey, new CacheEntry(code, Instant.now().plus(CODE_TTL)));

        // 메일 발송
        mailUtil.sendMail(email, code);
        log.info("email code {} → {}", code, email);
    }

    /**
     * 인증번호 검증
     **/
    @Transactional
    public void verify(EmailVerifyRequestDTO dto, String loginEmail) {
        String email = dto.getEmail();
        String codeKey = PREFIX + email;

        CacheEntry entry = codeCache.get(codeKey);
        if (entry == null || entry.isExpired()) {
            codeCache.remove(codeKey);
            throw new BusinessException(ErrorCode.CERTIFICATION_EXPIRED);
        }
        if (!entry.value.equals(dto.getCertificationCode())) {
            throw new BusinessException(ErrorCode.CERTIFICATION_MISMATCH);
        }

        // 검증 성공 시 사용자에 이메일 인증 저장
        User user = userRepository.findByEmail(loginEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.setCertificationEmail(email);
        // 사용 후 코드 제거
        codeCache.remove(codeKey);
        log.debug("✅ {} verified & saved to user {}", email, loginEmail);
    }

    /**
     * 간단한 락 획득: 없거나 만료된 경우만 성공
     **/
    private boolean acquireLock(String key, Duration ttl) {
        Instant now = Instant.now();
        CacheEntry existing = lockCache.get(key);

        if (existing != null && !existing.isExpired()) {
            return false;
        }
        lockCache.put(key, new CacheEntry("1", now.plus(ttl)));
        return true;
    }

    /**
     * 만료된 캐시 제거
     **/
    private void cleanUp() {
        Instant now = Instant.now();
        codeCache.entrySet().removeIf(e -> e.getValue().isExpiredAt(now));
        lockCache.entrySet().removeIf(e -> e.getValue().isExpiredAt(now));
    }

    private String generate() {
        int bound = (int) Math.pow(10, CODE_LEN);
        return String.format("%0" + CODE_LEN + "d", random.nextInt(bound));
    }

    /**
     * 캐시 엔트리 클래스
     **/
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
