package project.luckybooky.domain.admin.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class EventCreatedWebhookEvent extends ApplicationEvent {
    private final Long eventId;

    public EventCreatedWebhookEvent(Object source, Long eventId) {
        super(source);
        this.eventId = eventId;
    }
}
