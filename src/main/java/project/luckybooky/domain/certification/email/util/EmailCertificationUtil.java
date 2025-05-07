package project.luckybooky.domain.certification.email.util;


import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailCertificationUtil {

    private final JavaMailSender mailSender;
    @Value("${app.email.from}")
    private String from;

    public void sendMail(String to, String code) {
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, "UTF-8");
        helper.setTo(to);
        helper.setFrom(from);
        helper.setSubject("[무비부키] 이메일 인증번호");
        helper.setText("""
            <p>안녕하세요, 무비부키입니다.</p>
            <p>아래 인증번호를 입력해 주세요.</p>
            <h2>%s</h2>
            <p>(유효 시간: 3분)</p>
        """.formatted(code), true);
        mailSender.send(msg);
    }
}

