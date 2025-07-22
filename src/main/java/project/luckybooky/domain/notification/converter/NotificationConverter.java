package project.luckybooky.domain.notification.converter;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.time.LocalDateTime;
import project.luckybooky.domain.notification.entity.NotificationInfo;
import project.luckybooky.domain.notification.type.HostNotificationType;
import project.luckybooky.domain.notification.type.ParticipantNotificationType;
import project.luckybooky.domain.user.entity.User;

public class NotificationConverter {

    // 호스트 관련 알림 전송 converter
    public static Message toFcmMessage(User user, HostNotificationType hostType, String eventName, Long eventId) {
        String token = user.getFcmToken();
        if (token == null || token.isBlank()) {
            return null;
        }

        if (hostType == HostNotificationType.EVENT_CREATED) {
            return Message.builder()
                    .setToken(token)
                    .putData("type", hostType.name())
                    .putData("title", hostType.getTitle())
                    .putData("body", hostType.formatBody(eventName))
                    .putData("eventId", String.valueOf(eventId))
                    .build();
        }

        return Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(hostType.getTitle())
                        .setBody(hostType.formatBody(eventName))
                        .build())
                .putData("eventId", String.valueOf(eventId))
                .build();
    }

    // 테스트용 알림 전송 converter
    public static Message toMessage(User user, String title, String body) {
        String token = user.getFcmToken();
        if (token == null || token.isBlank()) {
            return null;
        }
        return Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();
    }

    public static Message toFcmMessageParticipant(User user, ParticipantNotificationType type, String eventName,
                                                  Long eventId) {
        if (user.getFcmToken() == null) {
            return null;
        }
        return Message.builder()
                .setToken(user.getFcmToken())
                .setNotification(Notification.builder()
                        .setTitle(type.getTitle())
                        .setBody(type.formatBody(eventName))
                        .build())
                .putData("eventId", String.valueOf(eventId))
                .build();
    }

    public static NotificationInfo toEntityParticipant(
            User user,
            ParticipantNotificationType type,
            String eventName,
            Long eventId
    ) {
        return NotificationInfo.builder()
                .user(user)
                .title(type.getTitle())
                .body(type.formatBody(eventName))
                .eventId(eventId)
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .build();
    }

    public static NotificationInfo toEntity(
            User user,
            HostNotificationType type,
            String eventName,
            Long eventId
    ) {
        return NotificationInfo.builder()
                .user(user)
                .title(type.getTitle())
                .body(type.formatBody(eventName))
                .eventId(eventId)
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .build();
    }


}
