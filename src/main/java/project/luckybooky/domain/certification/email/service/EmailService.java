package project.luckybooky.domain.certification.email.service;

import jakarta.transaction.Transactional;
import java.security.SecureRandom;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import project.luckybooky.domain.certification.email.dto.request.EmailRequestDTO;
import project.luckybooky.domain.certification.email.dto.request.EmailVerifyRequestDTO;
import project.luckybooky.domain.certification.email.util.EmailCertificationUtil;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.repository.UserRepository;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;
import project.luckybooky.global.redis.SmsCertificationCache;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private static final int CODE_LEN = 4;
    private static final Duration CODE_TTL = Duration.ofMinutes(3);
    private static final Duration LOCK_TTL = Duration.ofSeconds(10);
    private static final String PREFIX = "otp:email:";

    private final EmailCertificationUtil mailUtil;
    private final SmsCertificationCache cache;
    private final UserRepository userRepository;
    private final SecureRandom random = new SecureRandom();

    /**
     * 인증번호 발송
     **/
    public void sendCode(EmailRequestDTO dto) {
        String email = dto.getEmail();
        String lockKey = PREFIX + email + ":lock";
        String codeKey = PREFIX + email;

        // 10초 동안 중복 요청 제한
        if (!cache.store(lockKey, "1", LOCK_TTL)) {
            throw new BusinessException(ErrorCode.CERTIFICATION_DUPLICATED);
        }

        cache.remove(codeKey);
        String code = generate();
        cache.store(codeKey, code, CODE_TTL);

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
        String saved = cache.get(codeKey);

        if (saved == null) {
            throw new BusinessException(ErrorCode.CERTIFICATION_EXPIRED);
        }
        if (!saved.equals(dto.getCertificationCode())) {
            throw new BusinessException(ErrorCode.CERTIFICATION_MISMATCH);
        }

        User user = userRepository.findByEmail(loginEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.setCertificationEmail(email);
        cache.remove(codeKey);
        log.debug("✅ {} verified & saved to user {}", email, loginEmail);
    }

    private String generate() {
        int bound = (int) Math.pow(10, CODE_LEN);
        return String.format("%0" + CODE_LEN + "d", random.nextInt(bound));
    }
}
