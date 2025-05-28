package project.luckybooky.domain.certification.email.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailCertificationUtil {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.email.from}")
    private String from;

    @Async
    public void sendMail(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // true: multipart
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    StandardCharsets.UTF_8.name()
            );

            // Thymeleaf 렌더링
            Context ctx = new Context();
            ctx.setVariable("code", code);
            String html = templateEngine.process("email-certification", ctx);

            helper.setTo(to);
            helper.setFrom(from, "무비부키");
            helper.setSubject("[무비부키] 이메일 인증번호");
            helper.setText(html, true);

            // 로고를 클래스패스 리소스로 inline 첨부 (CID: logoImage)
            ClassPathResource logo = new ClassPathResource("templates/logo.png");
            helper.addInline("logoImage", logo);

            mailSender.send(message);
            log.debug("인증 메일 전송 → {}", to);

        } catch (MessagingException | MailException | UnsupportedEncodingException e) {
            log.error("❌ 이메일 전송 실패. to={}, cause={}", to, e.getMessage(), e);
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAIL);
        }
    }
}
