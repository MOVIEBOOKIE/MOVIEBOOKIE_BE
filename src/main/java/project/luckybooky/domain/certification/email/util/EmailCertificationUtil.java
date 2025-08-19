package project.luckybooky.domain.certification.email.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailCertificationUtil {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.email.from}")
    private String from;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    @Async
    public void sendMail(String to, String code) {
        try {
            // 이메일 형식 검증
            validateEmailFormat(to);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    "UTF-8"
            );

            Context ctx = new Context();
            ctx.setVariable("code", code);

            String html;
            try {
                html = templateEngine.process("email-certification", ctx);
            } catch (Exception e) {
                log.error("이메일 템플릿 처리 실패: to={}, error={}", to, e.getMessage(), e);
                throw new BusinessException(ErrorCode.EMAIL_SEND_FAIL);
            }

            helper.setTo(to);
            helper.setFrom(from, "무비부키");
            helper.setSubject("[무비부키] 이메일 인증번호");
            helper.setText(html, true);

            try {
                ClassPathResource logo = new ClassPathResource("static/images/logo.png");
                helper.addInline("logoImage", logo);
            } catch (Exception e) {
                log.warn("로고 이미지 추가 실패: {}", e.getMessage());
            }

            mailSender.send(message);
            log.debug("인증 메일 전송 성공 → {}", to);

        } catch (MessagingException e) {
            log.error("이메일 메시지 생성 실패: to={}, error={}", to, e.getMessage(), e);
            handleEmailError(e);
        } catch (MailException e) {
            log.error("이메일 전송 실패: to={}, error={}", to, e.getMessage(), e);
            handleEmailError(e);
        } catch (UnsupportedEncodingException e) {
            log.error("이메일 인코딩 오류: to={}, error={}", to, e.getMessage(), e);
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAIL);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("이메일 전송 중 예상치 못한 오류: to={}, error={}", to, e.getMessage(), e);
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAIL);
        }
    }

    private void validateEmailFormat(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.EMAIL_INVALID_FORMAT);
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            log.error("잘못된 이메일 형식: {}", email);
            throw new BusinessException(ErrorCode.EMAIL_INVALID_FORMAT);
        }
    }

    private void handleEmailError(Exception e) {
        String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        String fullErrorMessage = e.toString().toLowerCase();

        log.error("이메일 전송 에러: {}", e.getMessage());

        if (fullErrorMessage.contains("quota") || fullErrorMessage.contains("할당량") || fullErrorMessage.contains("limit")
                || fullErrorMessage.contains("552")) {
            log.error("이메일 할당량 초과: {}", e.getMessage());
            throw new BusinessException(ErrorCode.EMAIL_RATE_LIMIT_EXCEEDED);
        } else if (fullErrorMessage.contains("authentication") || fullErrorMessage.contains("인증")
                || fullErrorMessage.contains("535")) {
            log.error("이메일 인증 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.EMAIL_SERVER_ERROR);
        } else if (fullErrorMessage.contains("mailbox not found") || fullErrorMessage.contains("메일박스 없음")
                || fullErrorMessage.contains("550")) {
            log.error("이메일 메일박스 없음: {}", e.getMessage());
            throw new BusinessException(ErrorCode.EMAIL_INVALID_FORMAT);
        } else if (fullErrorMessage.contains("timeout") || fullErrorMessage.contains("타임아웃")) {
            log.error("이메일 타임아웃: {}", e.getMessage());
            throw new BusinessException(ErrorCode.EMAIL_SERVER_ERROR);
        } else if (fullErrorMessage.contains("connection") || fullErrorMessage.contains("연결")) {
            log.error("이메일 연결 오류: {}", e.getMessage());
            throw new BusinessException(ErrorCode.EMAIL_SERVER_ERROR);
        } else {
            log.error("이메일 전송 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAIL);
        }
    }
}
