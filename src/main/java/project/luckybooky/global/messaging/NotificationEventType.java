package project.luckybooky.global.messaging;

public final class NotificationEventType {
    private NotificationEventType() {
    }

    public static final String HOST_PUSH = "HOST_PUSH";
    public static final String PARTICIPANT_PUSH = "PARTICIPANT_PUSH";
    public static final String DIRECT_PUSH = "DIRECT_PUSH";
    public static final String VENUE_CONFIRMED_MAIL = "VENUE_CONFIRMED_MAIL";
    public static final String VENUE_REJECTED_MAIL = "VENUE_REJECTED_MAIL";
}
