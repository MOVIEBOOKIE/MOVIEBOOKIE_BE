package project.luckybooky.domain.notification.event.app;

import lombok.Getter;
import project.luckybooky.domain.notification.type.HostNotificationType;

@Getter
public class HostNotificationEvent {
    private final Long eventId;
    private final Long hostUserId;
    private final HostNotificationType type;
    private final String eventName;

    public HostNotificationEvent(Long eventId, Long hostUserId, HostNotificationType type, String eventName) {
        this.eventId = eventId;
        this.hostUserId = hostUserId;
        this.type = type;
        this.eventName = eventName;
    }
}
