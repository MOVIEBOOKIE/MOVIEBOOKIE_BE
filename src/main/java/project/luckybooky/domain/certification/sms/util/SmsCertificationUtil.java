package project.luckybooky.domain.certification.sms.util;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.exception.NurigoUnknownException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

import java.util.regex.Pattern;

@Slf4j
@Component
public class SmsCertificationUtil {

    @Value("${coolsms.api-key}")
    private String apiKey;

    @Value("${coolsms.api-secret}")
    private String apiSecret;

    @Value("${coolsms.from-number}")
    private String fromNumber;

    private static final Pattern PHONE_PATTERN = Pattern.compile("^01[0-9]-?[0-9]{4}-?[0-9]{4}$");
    private static final int MAX_MESSAGE_LENGTH = 90; // SMS 최대 길이

    DefaultMessageService messageService;

    @PostConstruct
    public void init() {
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
    }

    public void sendSMS(String to, String certificationCode) {
        try {
            // 전화번호 형식 검증
            validatePhoneNumber(to);
            
            // 메시지 생성
            String messageText = "[무비부키] 본인확인 인증번호는 " + certificationCode + "입니다.";
            
            // 메시지 길이 검증
            if (messageText.length() > MAX_MESSAGE_LENGTH) {
                log.error("SMS 메시지가 너무 깁니다. length: {}", messageText.length());
                throw new BusinessException(ErrorCode.SMS_MESSAGE_TOO_LONG);
            }

            Message message = new Message();
            message.setFrom(fromNumber);
            message.setTo(to);
            message.setText(messageText);

            log.debug("SMS 전송 시도: {} → {}", fromNumber, to);
            
            SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
            
            // 응답 상태 확인
            if (response != null) {
                log.debug("SMS 전송 성공: messageId={}", response.getMessageId());
            } else {
                log.error("SMS 전송 실패: 응답이 null입니다.");
                throw new BusinessException(ErrorCode.SMS_SEND_FAIL);
            }

        } catch (Exception e) {
            log.error("SMS 전송 중 오류: to={}, error={}", to, e.getMessage(), e);
            
            // 일반적인 SMS 전송 실패로 처리
            if (e.getMessage() != null) {
                String errorMessage = e.getMessage().toLowerCase();
                if (errorMessage.contains("잔액") || errorMessage.contains("balance")) {
                    throw new BusinessException(ErrorCode.SMS_INSUFFICIENT_BALANCE);
                } else if (errorMessage.contains("발신번호") || errorMessage.contains("from")) {
                    throw new BusinessException(ErrorCode.SMS_INVALID_SENDER_NUMBER);
                } else if (errorMessage.contains("수신번호") || errorMessage.contains("to")) {
                    throw new BusinessException(ErrorCode.SMS_INVALID_PHONE_FORMAT);
                } else if (errorMessage.contains("차단") || errorMessage.contains("block")) {
                    throw new BusinessException(ErrorCode.SMS_BLOCKED_NUMBER);
                } else if (errorMessage.contains("길이") || errorMessage.contains("length")) {
                    throw new BusinessException(ErrorCode.SMS_MESSAGE_TOO_LONG);
                } else if (errorMessage.contains("제한") || errorMessage.contains("limit")) {
                    throw new BusinessException(ErrorCode.SMS_RATE_LIMIT_EXCEEDED);
                }
            }
            
            throw new BusinessException(ErrorCode.SMS_SEND_FAIL);
        }
    }

    private void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.SMS_INVALID_PHONE_FORMAT);
        }
        
        // 하이픈 제거 후 검증
        String cleanPhone = phoneNumber.replaceAll("-", "");
        if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
            log.error("잘못된 전화번호 형식: {}", phoneNumber);
            throw new BusinessException(ErrorCode.SMS_INVALID_PHONE_FORMAT);
        }
    }


}
