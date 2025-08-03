package project.luckybooky.domain.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import project.luckybooky.domain.notification.dto.ConfirmedData;
import project.luckybooky.domain.notification.dto.RejectedData;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@Service
@RequiredArgsConstructor
public class MailTemplateService {

    @Value("${app.home-url}")
    private String homeUrl;

    @Value("${mail.from:no-reply@luckybooky.com}")
    private String fromAddress;

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final ResourceLoader resourceLoader;

    public void sendVenueConfirmedMail(String to, ConfirmedData data) {
        Context ctx = new Context();
        ctx.setVariables(Map.of(
                "mediaTitle", data.getMediaTitle(),
                "eventTitle", data.getEventTitle(),
                "hostName", data.getHostName(),
                "date", formatDate(data),
                "time", formatTime(data),
                "venue", data.getLocationName(),
                "capacity", formatCapacity(data.getMaxParticipants()),
                "contact", data.getContact(),
                "participantsLink", homeUrl + "/events/" + data.getEventId() + "/participants",
                "homeUrl", homeUrl
        ));

        // 2) 템플릿과 CID 자원 정보 전달
        sendTemplateMail(
                to,
                "[MovieBookie] 대관 확정 안내: " + data.getEventTitle(),
                "venue_confirmed",
                ctx,
                new InlineResource("logoCid", "classpath:images/logo.png"),
                new InlineResource("groupChatCid", "classpath:images/groupChat.png"),
                new InlineResource("chatCid", "classpath:images/chat.png")
        );
    }

    public void sendVenueRejectedMail(String to, RejectedData data) {
        Context ctx = new Context();
        ctx.setVariables(Map.of(
                "eventTitle", Optional.ofNullable(data.getEventTitle()).orElse(""),
                "hostName", Optional.ofNullable(data.getHostName()).orElse("주최자님"),
                "homeUrl", homeUrl
        ));

        sendTemplateMail(
                to,
                "[MovieBookie] 대관 승인 실패 안내: "
                        + ctx.getVariable("eventTitle"),
                "venue_rejected",
                ctx,
                new InlineResource("logoCid", "classpath:images/logo.png")
        );
    }

    private void sendTemplateMail(
            String to,
            String subject,
            String templateName,
            Context ctx,
            InlineResource... inlineResources
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setFrom(fromAddress);
            helper.setSubject(subject);

            String html = templateEngine.process(templateName, ctx);
            helper.setText(html, true);

            for (InlineResource res : inlineResources) {
                Resource resource = resourceLoader.getResource(res.path);
                helper.addInline(res.cid, resource);
            }

            mailSender.send(message);
        } catch (MessagingException ex) {
            throw new BusinessException(ErrorCode.MAIL_SEND_FAILED);
        }
    }

    private String formatDate(ConfirmedData d) {
        return d.getEventDate()
                .format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
                + " (" + d.getEventDay() + ")";
    }

    private String formatTime(ConfirmedData d) {
        return d.getEventStartTime() + " - " + d.getEventEndTime();
    }

    private String formatCapacity(Integer max) {
        return (max != null)
                ? max + "명 (주최자 포함)"
                : "정보 없음";
    }

    /**
     * 내부에서만 쓰이는 CID + 리소스 경로 묶음
     */
    private static record InlineResource(String cid, String path) {
    }
}
