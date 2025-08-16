package project.luckybooky.domain.notification.event.app;

import lombok.Getter;
import project.luckybooky.domain.notification.type.ParticipantNotificationType;

@Getter
public class ParticipantNotificationEvent {
    private final Long eventId;
    private final Long userId;
    private final ParticipantNotificationType type;
    private final String eventName;

    public ParticipantNotificationEvent(Long eventId, Long userId, ParticipantNotificationType type, String eventName) {
        this.eventId = eventId;
        this.userId = userId;
        this.type = type;
        this.eventName = eventName;
    }
}
