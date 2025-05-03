package project.luckybooky.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import project.luckybooky.domain.user.entity.GroupType;
import project.luckybooky.domain.user.entity.UserType;

@Getter
@AllArgsConstructor
@Builder
public class UserTypeAssignResponse {
    private String userTypeCode;
    private String userTypeLabel;
    private String description;
}
