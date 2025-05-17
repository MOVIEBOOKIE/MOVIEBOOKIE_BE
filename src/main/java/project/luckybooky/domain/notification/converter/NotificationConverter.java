package project.luckybooky.domain.notification.converter;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import project.luckybooky.domain.notification.dto.request.NotificationRequestDTO;
import project.luckybooky.domain.user.entity.User;

public class NotificationConverter {

    public static Message toFcmMessage(User user, NotificationRequestDTO dto) {
        return Message.builder()
                .setToken(user.getFcmToken())
                .setNotification(Notification.builder()
                        .setTitle(dto.getTitle())
                        .setBody(dto.getBody())
                        .build())
                .build();
    }
}
