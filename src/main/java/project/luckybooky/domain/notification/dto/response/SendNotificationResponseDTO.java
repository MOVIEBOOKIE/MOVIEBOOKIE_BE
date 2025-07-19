package project.luckybooky.domain.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SendNotificationResponseDTO {
    private String status;
    private String message;
}

