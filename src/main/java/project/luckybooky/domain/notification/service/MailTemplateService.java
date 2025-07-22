package project.luckybooky.domain.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import project.luckybooky.domain.notification.dto.ConfirmedData;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@Service
@RequiredArgsConstructor
public class MailTemplateService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final ResourceLoader resourceLoader;

    public void sendVenueConfirmedMail(String to, ConfirmedData data) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 기본 설정
            helper.setTo(to);
            helper.setSubject("[MovieBookie] 대관 확정 안내: " + data.getEventTitle());
            helper.setFrom("no-reply@luckybooky.com");

            // Thymeleaf Context
            Context ctx = new Context();
            ctx.setVariable("mediaTitle", data.getMediaTitle());
            ctx.setVariable("eventTitle", data.getEventTitle());
            String dateStr = data.getEventDate().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
                    + " (" + data.getEventDay() + ") "
                    + data.getEventStartTime() + " - " + data.getEventEndTime();
            ctx.setVariable("date", dateStr);
            ctx.setVariable("venue", data.getLocationName());
            ctx.setVariable("capacity", data.getMaxParticipants());
            ctx.setVariable("contact", data.getContact());
            ctx.setVariable("participantsLink", data.getParticipantsLink());

            // HTML 렌더링
            String html = templateEngine.process("venue_confirmed", ctx);
            helper.setText(html, true);

            // 인라인 이미지
            Resource logo = resourceLoader.getResource("classpath:templates/logo.png");
            helper.addInline("logoCid", logo);
            mailSender.send(message);

        } catch (MessagingException ex) {
            throw new BusinessException(ErrorCode.MAIL_SEND_FAILED);
        }
    }
}
