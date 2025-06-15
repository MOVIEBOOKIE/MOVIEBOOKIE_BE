package project.luckybooky.domain.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ParticipantDTO {
    private Long userId;
    private String username;
    private String profileImageUrl;
}
