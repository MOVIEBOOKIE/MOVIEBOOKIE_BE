package project.luckybooky.domain.notification.converter;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import project.luckybooky.domain.notification.type.HostNotificationType;
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


}
