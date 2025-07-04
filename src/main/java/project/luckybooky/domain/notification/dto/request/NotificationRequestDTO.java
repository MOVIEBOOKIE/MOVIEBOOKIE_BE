package project.luckybooky.domain.notification.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NotificationRequestDTO {
    private String title;
    private String body;
}
