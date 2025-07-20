package project.luckybooky.domain.notification.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDTO {
    private String title;
    private String body;
    private Long eventId;
}
