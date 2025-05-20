package project.luckybooky.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.luckybooky.domain.user.entity.ContentCategory;
import project.luckybooky.domain.user.entity.Step1Question;
import project.luckybooky.domain.user.entity.Step2Question;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTypeAssignRequest {

    @Schema(description = "STEP 1 – 오늘 컨디션", required = true)
    private Step1Question step1Question;

    @Schema(description = "STEP 2 – 콘텐츠 선택 기준", required = true)
    private Step2Question step2Question;

    @Schema(description = "STEP 3 – 선호 콘텐츠 유형", required = true)
    private ContentCategory favoriteCategory;
}

