package project.luckybooky.domain.certification.email.service;

import java.security.SecureRandom;
import java.time.Duration;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import project.luckybooky.domain.certification.email.dto.request.EmailRequestDTO;
import project.luckybooky.domain.certification.email.dto.request.EmailVerifyRequestDTO;
import project.luckybooky.domain.certification.email.util.EmailCertificationUtil;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.repository.UserRepository;
import project.luckybooky.domain.user.util.AuthenticatedUserUtils;
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
    private final SmsCertificationCache  cache;
    private final UserRepository         userRepository;
    private final SecureRandom           random = new SecureRandom();

    /* 1) 인증번호 발송 */
    public void sendCode(EmailRequestDTO dto) {
        String code = generate();
        if (!cache.store(PREFIX + dto.getEmail(), code, TTL)) {
            throw new BusinessException(ErrorCode.CERTIFICATION_DUPLICATED);
        }
        mailUtil.sendMail(dto.getEmail(), code);
        log.debug("email code {} → {}", code, dto.getEmail());
    }

    /* 2) 인증번호 검증 */
    @Transactional
    public void verify(EmailVerifyRequestDTO dto) {
        String key   = PREFIX + dto.getEmail();
        String saved = cache.get(key);

        if (saved == null)
            throw new BusinessException(ErrorCode.CERTIFICATION_EXPIRED);
        if (!saved.equals(dto.getCertificationCode()))
            throw new BusinessException(ErrorCode.CERTIFICATION_MISMATCH);

        String loginEmail = AuthenticatedUserUtils.getAuthenticatedUserEmail();
        User user = userRepository.findByEmail(loginEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.setCertificationEmail(dto.getEmail());

        cache.remove(key);
        log.debug("✅ {} verified & saved to user {}", dto.getEmail(), loginEmail);
    }

    private String generate() {
        int bound = (int) Math.pow(10, CODE_LEN);
        return String.format("%0" + CODE_LEN + "d", random.nextInt(bound));
    }
}
