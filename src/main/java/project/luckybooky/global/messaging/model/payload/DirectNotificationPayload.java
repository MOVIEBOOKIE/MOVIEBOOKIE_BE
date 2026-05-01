package project.luckybooky.global.messaging.model.payload;

public record DirectNotificationPayload(
        Long userId,
        String title,
        String body,
        Long eventId
) {
}
