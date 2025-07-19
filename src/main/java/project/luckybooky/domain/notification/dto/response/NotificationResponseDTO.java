package project.luckybooky.domain.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import project.luckybooky.domain.notification.entity.NotificationInfo;

@Getter
@AllArgsConstructor
public class NotificationResponseDTO {
    private String status;
    private String message;

    public static NotificationResponseDTO fromEntity(NotificationInfo notification) {
        return new NotificationResponseDTO(
                notification.getStatus().name().toLowerCase(),
                notification.getBody()
        );
    }
}


