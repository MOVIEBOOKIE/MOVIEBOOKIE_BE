package project.luckybooky.domain.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ParticipantNotificationPreviewDTO {
    private final String title;
    private final String body;
}
