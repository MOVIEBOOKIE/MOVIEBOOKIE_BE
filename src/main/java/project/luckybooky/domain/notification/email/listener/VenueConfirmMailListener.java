package project.luckybooky.domain.notification.email.listener;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.notification.email.event.EventVenueConfirmedEvent;
import project.luckybooky.domain.notification.email.service.MailTemplateService;
import project.luckybooky.domain.participation.service.ParticipationService;

/**
 * 대관 확정 후 AfterCommit 단계에서 비동기로 이메일 전송
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VenueConfirmMailListener {

    private final ParticipationService participationService;
    private final MailTemplateService templateService;
    private final JavaMailSender mailSender;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onConfirmed(EventVenueConfirmedEvent event) {
        Event e = event.event();
        List<String> recipients = participationService.findAllEmailsByEventId(e.getId());

        String subject = "🎉 '" + e.getEventTitle() + "' 대관 확정 안내";
        String htmlBody = templateService.renderVenueConfirmed(e, event.ticketId());

        for (String to : recipients) {
            sendMail(to, subject, htmlBody);
        }
        log.info("대관 확정 메일 총 {}건 발송 완료", recipients.size());
    }

    private void sendMail(String to, String subject, String html) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(msg);
        } catch (MessagingException | MailException ex) {
            log.error("메일 전송 실패 to={} cause={}", to, ex.getMessage(), ex);
        }
    }
}
