package project.luckybooky.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import project.luckybooky.domain.user.entity.GroupType;
import project.luckybooky.domain.user.entity.UserType;

@Getter
@AllArgsConstructor
public class UserTypeAssignResponse {
    private String     userTypeCode;   // 예) MOVIE_TRENDY_VIEWER
    private String     userTypeLabel;  // 예) 🍿 핫플릭스만 골라보는 감각 감상러
    private GroupType  groupType;      // A · B
}
