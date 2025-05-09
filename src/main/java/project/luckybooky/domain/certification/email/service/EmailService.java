package project.luckybooky.domain.certification.email.service;

import java.security.SecureRandom;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import project.luckybooky.domain.certification.email.dto.request.EmailRequestDTO;
import project.luckybooky.domain.certification.email.dto.request.EmailVerifyRequestDTO;
import project.luckybooky.domain.certification.email.util.EmailCertificationUtil;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;
import project.luckybooky.global.redis.SmsCertificationCache;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private static final int CODE_LEN = 4;
    private static final Duration TTL = Duration.ofMinutes(3);
    private static final String PREFIX = "otp:email:";

    private final EmailCertificationUtil mailUtil;
    private final SmsCertificationCache cache;
    private final SecureRandom random = new SecureRandom();

    /* 1) 발송 */
    public void sendCode(EmailRequestDTO dto) {
        String code = generate();
        if (!cache.store(PREFIX + dto.getEmail(), code, TTL)) {
            throw new BusinessException(ErrorCode.CERTIFICATION_DUPLICATED);
        }
        mailUtil.sendMail(dto.getEmail(), code);
        log.debug("email code {} → {}", code, dto.getEmail());
    }

    /* 2) 검증 */
    public void verify(EmailVerifyRequestDTO dto) {
        String key = PREFIX + dto.getEmail();
        String saved = cache.get(key);

        if (saved == null) throw new BusinessException(ErrorCode.CERTIFICATION_EXPIRED);
        if (!saved.equals(dto.getCertificationCode()))
            throw new BusinessException(ErrorCode.CERTIFICATION_MISMATCH);

        cache.remove(key);
        log.debug("✅ {} verified", dto.getEmail());
    }

    private String generate() {
        int bound = (int) Math.pow(10, CODE_LEN);   // 10 000
        return String.format("%0" + CODE_LEN + "d", random.nextInt(bound));
    }
}

