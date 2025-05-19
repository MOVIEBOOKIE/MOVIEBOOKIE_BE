package project.luckybooky.domain.notification.listener;

import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import project.luckybooky.domain.notification.converter.NotificationConverter;
import project.luckybooky.domain.notification.event.HostNotificationEvent;
import project.luckybooky.domain.notification.service.NotificationService;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.repository.UserRepository;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@Component
@RequiredArgsConstructor
public class HostNotificationListener {
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @EventListener
    public void onHostNotification(HostNotificationEvent evt) {
        User host = userRepository.findById(evt.getHostUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Message msg = NotificationConverter.toFcmMessage(
                host, evt.getType(), evt.getEventName()
        );
        notificationService.send(msg);
    }
}

