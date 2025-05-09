package project.luckybooky.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import project.luckybooky.domain.user.entity.ContentCategory;
import project.luckybooky.domain.user.entity.Step1Question;
import project.luckybooky.domain.user.entity.Step2Question;

@Getter
@Builder
public class UserTypeAssignRequest {

    @Schema(description = "STEP 1 – 오늘 컨디션", implementation = Step1Question.class)
    private Step1Question step1Question;

    @Schema(description = "STEP 2 – 콘텐츠 선택 기준", implementation = Step2Question.class)
    private Step2Question step2Question;

    @Schema(description = "STEP 3 – 선호 콘텐츠", implementation = ContentCategory.class)
    private ContentCategory favoriteCategory;
}
