package project.luckybooky.domain.certification.email.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
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

import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

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
            
            // 템플릿 처리
            String html;
            try {
                html = templateEngine.process("email-certification", ctx);
            } catch (Exception e) {
                log.error("이메일 템플릿 처리 실패: to={}, error={}", to, e.getMessage(), e);
                throw new BusinessException(ErrorCode.EMAIL_TEMPLATE_ERROR);
            }

            helper.setTo(to);
            helper.setFrom(from, "무비부키");
            helper.setSubject("[무비부키] 이메일 인증번호");
            helper.setText(html, true);

            // 로고 이미지 추가
            try {
                ClassPathResource logo = new ClassPathResource("static/images/logo.png");
                helper.addInline("logoImage", logo);
            } catch (Exception e) {
                log.warn("로고 이미지 추가 실패: {}", e.getMessage());
                // 로고 추가 실패는 치명적이지 않으므로 계속 진행
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
            throw e; // 이미 BusinessException인 경우 그대로 던짐
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
        
        if (errorMessage.contains("authentication") || errorMessage.contains("인증")) {
            throw new BusinessException(ErrorCode.EMAIL_SERVER_ERROR);
        } else if (errorMessage.contains("quota") || errorMessage.contains("할당량")) {
            throw new BusinessException(ErrorCode.EMAIL_RATE_LIMIT_EXCEEDED);
        } else if (errorMessage.contains("invalid") || errorMessage.contains("잘못된")) {
            throw new BusinessException(ErrorCode.EMAIL_INVALID_FORMAT);
        } else if (errorMessage.contains("connection") || errorMessage.contains("연결")) {
            throw new BusinessException(ErrorCode.EMAIL_SERVER_ERROR);
        } else {
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAIL);
        }
    }
}
