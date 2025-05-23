package project.luckybooky.domain.notification.event;

import lombok.Getter;
import project.luckybooky.domain.notification.type.ParticipantNotificationType;

@Getter
public class ParticipantNotificationEvent {
    private final Long userId;
    private final ParticipantNotificationType type;
    private final String eventName;

    public ParticipantNotificationEvent(Long userId, ParticipantNotificationType type, String eventName) {
        this.userId = userId;
        this.type = type;
        this.eventName = eventName;
    }
}
