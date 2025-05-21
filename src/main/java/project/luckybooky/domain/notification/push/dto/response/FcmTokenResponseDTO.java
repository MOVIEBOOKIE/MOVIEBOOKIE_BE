package project.luckybooky.domain.notification.push.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FcmTokenResponseDTO {
    private String status;
    private String message;
}

