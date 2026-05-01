package project.luckybooky.global.messaging.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.luckybooky.domain.notification.type.HostNotificationType;
import project.luckybooky.domain.notification.type.ParticipantNotificationType;
import project.luckybooky.global.messaging.NotificationEventType;
import project.luckybooky.global.messaging.NotificationTopic;
import project.luckybooky.global.messaging.model.payload.DirectNotificationPayload;
import project.luckybooky.global.messaging.model.payload.HostNotificationPayload;
import project.luckybooky.global.messaging.model.payload.ParticipantNotificationPayload;
import project.luckybooky.global.messaging.model.payload.VenueConfirmedMailPayload;
import project.luckybooky.global.messaging.model.payload.VenueRejectedMailPayload;

@Service
@RequiredArgsConstructor
public class NotificationOutboxProducer {
    private final OutboxService outboxService;

    public void enqueueHostNotification(
            Long eventId,
            Long hostUserId,
            HostNotificationType type,
            String eventName
    ) {
        outboxService.enqueue(
                NotificationTopic.PUSH,
                NotificationEventType.HOST_PUSH,
                "host:" + hostUserId + ":event:" + eventId,
                String.valueOf(hostUserId),
                new HostNotificationPayload(eventId, hostUserId, type, eventName)
        );
    }

    public void enqueueParticipantNotification(
            Long eventId,
            Long userId,
            ParticipantNotificationType type,
            String eventName
    ) {
        outboxService.enqueue(
                NotificationTopic.PUSH,
                NotificationEventType.PARTICIPANT_PUSH,
                "participant:" + userId + ":event:" + eventId,
                String.valueOf(userId),
                new ParticipantNotificationPayload(eventId, userId, type, eventName)
        );
    }

    public void enqueueDirectNotification(
            Long userId,
            String title,
            String body,
            Long eventId
    ) {
        outboxService.enqueue(
                NotificationTopic.PUSH,
                NotificationEventType.DIRECT_PUSH,
                "direct:" + userId + ":" + (eventId == null ? "global" : eventId),
                String.valueOf(userId),
                new DirectNotificationPayload(userId, title, body, eventId)
        );
    }

    public void enqueueVenueConfirmedMail(Long eventId, Long hostUserId) {
        outboxService.enqueue(
                NotificationTopic.MAIL,
                NotificationEventType.VENUE_CONFIRMED_MAIL,
                "mail:confirmed:" + eventId + ":" + hostUserId,
                String.valueOf(hostUserId),
                new VenueConfirmedMailPayload(eventId, hostUserId)
        );
    }

    public void enqueueVenueRejectedMail(Long eventId, Long hostUserId) {
        outboxService.enqueue(
                NotificationTopic.MAIL,
                NotificationEventType.VENUE_REJECTED_MAIL,
                "mail:rejected:" + eventId + ":" + hostUserId,
                String.valueOf(hostUserId),
                new VenueRejectedMailPayload(eventId, hostUserId)
        );
    }
}
