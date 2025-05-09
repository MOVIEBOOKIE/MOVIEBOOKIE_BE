package project.luckybooky.domain.certification.email.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailCertificationUtil {

    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String from;

    @Async
    public void sendMail(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            // Spring 6: (mimeMessage, multipartMode, encoding)
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(to);
            helper.setFrom(from, "무비부키");   // 표시 이름 포함
            helper.setSubject("[무비부키] 이메일 인증번호");
            helper.setText(buildHtml(code), true); // true = HTML

            mailSender.send(message);
            log.debug("인증 메일 전송 → {}", to);

        } catch (MessagingException | MailException | UnsupportedEncodingException e) {
            log.error("❌ 이메일 전송 실패. to={}, cause={}", to, e.getMessage(), e);
            // 필요하다면 비즈니스 예외로 감싸 상위 계층에 전달
            throw new IllegalStateException("이메일 전송에 실패했습니다.", e);
        }
    }

    private String buildHtml(String code) {
        return """
               <p>안녕하세요, 무비부키입니다.</p>
               <p>아래 인증번호를 입력해 주세요.</p>
               <h2 style="letter-spacing:4px">%s</h2>
               <p>(유효 시간: 3분)</p>
               """.formatted(code);
    }
}