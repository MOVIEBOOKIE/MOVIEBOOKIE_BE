package project.luckybooky.global.messaging.model.payload;

import project.luckybooky.domain.notification.type.ParticipantNotificationType;

public record ParticipantNotificationPayload(
        Long eventId,
        Long userId,
        ParticipantNotificationType type,
        String eventName
) {
}
