package project.luckybooky.domain.certification.sms.Service;

import jakarta.transaction.Transactional;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import project.luckybooky.domain.certification.sms.dto.request.SmsRequestDTO;
import project.luckybooky.domain.certification.sms.dto.request.SmsVerifyRequestDTO;
import project.luckybooky.domain.certification.sms.util.SmsCertificationUtil;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.repository.UserRepository;
import project.luckybooky.domain.user.util.AuthenticatedUserUtils;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;
import project.luckybooky.global.redis.SmsCertificationCache;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    private static final int CODE_LEN = 4;
    private static final Duration TTL = Duration.ofMinutes(3);
    private static final String PREFIX = "otp:sms:";

    private final SmsCertificationUtil smsUtil;
    private final SmsCertificationCache cache;
    private final UserRepository userRepository;

    /* 1) 인증번호 발송 */
    public void sendCertificationCode(SmsRequestDTO dto) {
        String key = PREFIX + dto.getPhoneNum();
        String code = generate();

        cache.remove(key);
        cache.store(key, code, TTL);

        smsUtil.sendSMS(dto.getPhoneNum(), code);
        log.debug("sms code {} → {}", code, dto.getPhoneNum());
    }

    /* 2) 인증번호 검증 & 전화번호 저장 */
    @Transactional
    public void verifyCertificationCode(SmsVerifyRequestDTO dto) {

        String key = PREFIX + dto.getPhoneNum();
        String saved = cache.get(key);

        if (saved == null) {
            throw new BusinessException(ErrorCode.CERTIFICATION_EXPIRED);
        }
        if (!saved.equals(dto.getCertificationCode())) {
            throw new BusinessException(ErrorCode.CERTIFICATION_MISMATCH);
        }

        // 현재 로그인 사용자를 이메일로 식별
        String loginEmail = AuthenticatedUserUtils.getAuthenticatedUserEmail();
        User user = userRepository.findByEmail(loginEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 동일 전화번호가 다른 사용자에게 이미 등록돼 있는지 검사
        userRepository.findByPhoneNumber(dto.getPhoneNum())
                .filter(other -> !other.getId().equals(user.getId()))
                .ifPresent(any -> {
                    throw new BusinessException(ErrorCode.PHONE_ALREADY_USED);
                });

        /* 전화번호 저장 */
        user.setPhoneNumber(dto.getPhoneNum());

        cache.remove(key);        // 성공 후 1회성 삭제
        log.debug("✅ {} verified & saved to user {}", dto.getPhoneNum(), loginEmail);
    }

    /* 6자리 숫자 코드 생성 */
    private String generate() {
        int bound = (int) Math.pow(10, CODE_LEN);   // 1 000 000
        return String.format("%0" + CODE_LEN + "d",
                ThreadLocalRandom.current().nextInt(bound));
    }
}
