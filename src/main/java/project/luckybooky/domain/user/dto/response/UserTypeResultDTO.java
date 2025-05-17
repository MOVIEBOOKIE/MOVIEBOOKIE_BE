package project.luckybooky.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UserTypeResultDTO {
    private String    username;
    private String    userTypeCode;
    private String    userTypeLabel;
    private String    description;
}

