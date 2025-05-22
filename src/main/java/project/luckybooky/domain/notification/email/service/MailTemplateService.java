package project.luckybooky.domain.notification.email.service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import project.luckybooky.domain.event.entity.Event;

/**
 * 대관 확정 안내 메일용 HTML 템플릿 렌더링
 */
@Service
@RequiredArgsConstructor
public class MailTemplateService {

    private final TemplateEngine thymeleaf;

    public String renderVenueConfirmed(Event event, Long ticketId) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("title", event.getEventTitle());
        vars.put("date", event.getEventDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")));
        vars.put("place", event.getLocation());
        vars.put("ticketLink", "https://moviebooky.com/tickets/" + ticketId);

        Context ctx = new Context(Locale.getDefault(), vars);
        return thymeleaf.process("mail/venue-confirmed", ctx);
    }
}
