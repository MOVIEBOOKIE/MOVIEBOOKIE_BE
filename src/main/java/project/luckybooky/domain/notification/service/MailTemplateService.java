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
            ctx.setVariable("hostName", data.getHostName()); // hostName 필드 필요

            String dateStr = data.getEventDate().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
                    + " (" + data.getEventDay() + ") "
                    + data.getEventStartTime() + " - " + data.getEventEndTime();
            ctx.setVariable("date", dateStr);
            ctx.setVariable("venue", data.getLocationName());

            String capacityFormatted;
            if (data.getMaxParticipants() != null) {
                capacityFormatted = data.getMaxParticipants() + "명 (주최자 포함)";
            } else {
                capacityFormatted = "정보 없음";
            }
            ctx.setVariable("capacity", capacityFormatted);

            ctx.setVariable("contact", data.getContact());
            ctx.setVariable("participantsLink", data.getParticipantsLink());
            ctx.setVariable("homeUrl", "https://your-domain.com"); // 필요 시 동적으로

            // HTML 렌더링
            String html = templateEngine.process("venue_confirmed", ctx);
            helper.setText(html, true);

            // 인라인 이미지들
            Resource logo = resourceLoader.getResource("classpath:templates/logo.png");
            helper.addInline("logoCid", logo);

            Resource groupChat = resourceLoader.getResource("classpath:templates/groupChat.png");
            helper.addInline("groupChatCid", groupChat);

            Resource chat = resourceLoader.getResource("classpath:templates/chat.png");
            helper.addInline("chatCid", chat);

            mailSender.send(message);

        } catch (MessagingException ex) {
            throw new BusinessException(ErrorCode.MAIL_SEND_FAILED);
        }
    }

    public void sendVenueRejectedMail(String to, ConfirmedData data, String rejectionReason) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("[MovieBookie] 대관 승인 실패 안내: " + data.getEventTitle());
            helper.setFrom("no-reply@luckybooky.com");

            Context ctx = new Context();
            ctx.setVariable("eventTitle", data.getEventTitle());
            ctx.setVariable("hostName", data.getHostName());
            ctx.setVariable("rejectionReason", rejectionReason);
            ctx.setVariable("homeUrl", "https://your-domain.com"); // 실제 URL로 대체

            String html = templateEngine.process("venue_rejected", ctx);
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
