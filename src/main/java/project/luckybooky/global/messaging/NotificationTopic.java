package project.luckybooky.global.messaging;

public final class NotificationTopic {
    private NotificationTopic() {
    }

    public static final String PUSH = "notification.push.v1";
    public static final String MAIL = "notification.mail.v1";
    public static final String PUSH_DLQ = "notification.push.dlq.v1";
    public static final String MAIL_DLQ = "notification.mail.dlq.v1";
}
