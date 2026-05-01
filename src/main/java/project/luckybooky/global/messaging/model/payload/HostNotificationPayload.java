package project.luckybooky.global.messaging.model.payload;

import project.luckybooky.domain.notification.type.HostNotificationType;

public record HostNotificationPayload(
        Long eventId,
        Long hostUserId,
        HostNotificationType type,
        String eventName
) {
}
