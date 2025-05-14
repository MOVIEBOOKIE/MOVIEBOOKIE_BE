package project.luckybooky.domain.notification.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FcmTokenRequestDTO {
    private String token;
}
