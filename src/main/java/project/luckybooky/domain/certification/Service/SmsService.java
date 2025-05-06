package project.luckybooky.domain.certification.Service;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import project.luckybooky.domain.certification.dto.request.SmsRequestDTO;
import project.luckybooky.domain.certification.dto.request.SmsVerifyRequestDTO;
import project.luckybooky.domain.certification.util.SmsCertificationUtil;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;
import project.luckybooky.global.redis.SmsCertificationCache;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    private static final int CODE_LEN = 4;
    private static final Duration CODE_TTL = Duration.ofMinutes(3);


    private final SmsCertificationUtil smsCertificationUtil;
    private final SmsCertificationCache cache;


    /**
     * 인증번호(SMS) 발송
     */
    public void sendCertificationCode(SmsRequestDTO request) {
        String code = generateCode();
        cache.store(request.getPhoneNum(), code, CODE_TTL);
        smsCertificationUtil.sendSMS(request.getPhoneNum(), code);
        log.debug("SMS 인증번호 {} → {}", code, request.getPhoneNum());

        // TODO: 코드·전화번호를 Redis 등 임시 저장소에 저장 & 만료 시간 설정
    }


    private String generateCode() {
        int max = (int) Math.pow(10, CODE_LEN);
        return String.format("%0" + CODE_LEN + "d",
                ThreadLocalRandom.current().nextInt(max));
    }

    public void verifyCode(SmsVerifyRequestDTO dto) {
        String saved = cache.get(dto.getPhoneNum());

        if(saved == null){
            throw new BusinessException(ErrorCode.CERTIFICATION_EXPIRED);
        }
        if (!saved.equals(dto.getCertificationCode())) {
            throw new BusinessException(ErrorCode.CERTIFICATION_MISMATCH);
        }

        cache.remove(dto.getPhoneNum());
        log.debug("✅ phone {} 인증 성공", dto.getPhoneNum());
    }
}
