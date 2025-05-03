package project.luckybooky.domain.certification.service;   // ✅ 패키지명 소문자

import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import project.luckybooky.domain.certification.dto.request.SmsRequestDTO;
import project.luckybooky.domain.certification.util.SmsCertificationUtil;

@Service                      // ✅ 스프링 빈 등록
@RequiredArgsConstructor      // ✅ 생성자 자동 생성 → @Autowired 제거
@Slf4j
public class SmsService {

    private static final int CODE_LEN = 4;

    private final SmsCertificationUtil smsCertificationUtil;

    /**
     * 인증번호(SMS) 발송
     */
    public void sendCertificationCode(SmsRequestDTO request) {
        String code = generateCode();
        smsCertificationUtil.sendSMS(request.getPhoneNum(), code);   // 실제 발송
        log.debug("SMS 인증번호 {} → {}", code, request.getPhoneNum());

        // TODO: 코드·전화번호를 Redis 등 임시 저장소에 저장 & 만료 시간 설정
    }

    /** 6자리 인증번호 생성 */
    private String generateCode() {
        int max = (int) Math.pow(10, CODE_LEN);          // 1 000 000
        return String.format("%0" + CODE_LEN + "d",
                ThreadLocalRandom.current().nextInt(max));
    }
}
