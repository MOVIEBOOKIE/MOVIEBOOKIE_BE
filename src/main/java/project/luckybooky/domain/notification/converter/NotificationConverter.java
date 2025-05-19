package project.luckybooky.domain.notification.converter;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import project.luckybooky.domain.notification.type.HostNotificationType;
import project.luckybooky.domain.user.entity.User;

public class NotificationConverter {

    public static Message toFcmMessage(User user, HostNotificationType type, String eventName) {
        return Message.builder()
                .setToken(user.getFcmToken())
                .setNotification(Notification.builder()
                        .setTitle(type.getTitle())
                        .setBody(type.formatBody(eventName))
                        .build())
                .build();
    }

    public static Message toMessage(User user, String title, String body) {
        return Message.builder()
                .setToken(user.getFcmToken())
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();
    }
}
