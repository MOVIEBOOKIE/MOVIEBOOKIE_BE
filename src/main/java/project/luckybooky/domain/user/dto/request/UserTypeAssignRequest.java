package project.luckybooky.domain.user.dto.request;

import java.util.List;
import lombok.Getter;
import project.luckybooky.domain.user.entity.ContentCategory;
import project.luckybooky.domain.user.entity.Step1Question;
import project.luckybooky.domain.user.entity.Step2Question;

@Getter
public class UserTypeAssignRequest {
    private Step1Question step1Question;
    private Step2Question step2Question;
    private ContentCategory favoriteCategory;
}
