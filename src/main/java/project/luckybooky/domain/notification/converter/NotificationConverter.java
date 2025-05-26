package project.luckybooky.domain.notification.converter;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import project.luckybooky.domain.notification.type.HostNotificationType;
import project.luckybooky.domain.notification.type.ParticipantNotificationType;
import project.luckybooky.domain.user.entity.User;

public class NotificationConverter {

    // 호스트 관련 알림 전송 converter
    public static Message toFcmMessage(User user, HostNotificationType hostType, String eventName) {
        String token = user.getFcmToken();
        if (token == null || token.isBlank()) {
            return null;
        }
        return Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(hostType.getTitle())
                        .setBody(hostType.formatBody(eventName))
                        .build())
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

    public static Message toFcmMessageParticipant(User user, ParticipantNotificationType type, String eventName) {
        if (user.getFcmToken() == null) {
            return null;
        }
        return Message.builder()
                .setToken(user.getFcmToken())
                .setNotification(Notification.builder()
                        .setTitle(type.getTitle())
                        .setBody(type.formatBody(eventName))
                        .build())
                .build();
    }


}
