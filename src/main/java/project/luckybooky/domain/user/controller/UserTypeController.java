package project.luckybooky.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import project.luckybooky.domain.user.dto.request.UserTypeAssignRequest;
import project.luckybooky.domain.user.dto.response.UserTypeAssignResponse;
import project.luckybooky.domain.user.dto.response.UserTypeResultDTO;
import project.luckybooky.domain.user.entity.ContentCategory;
import project.luckybooky.domain.user.entity.Step1Question;
import project.luckybooky.domain.user.entity.Step2Question;
import project.luckybooky.domain.user.service.UserTypeService;
import project.luckybooky.global.apiPayload.response.CommonResponse;
import project.luckybooky.global.apiPayload.response.ResultCode;

@Tag(name = "User", description = "사용자 유형 검사 API")
@RestController
@RequestMapping("/api/user-type")
@RequiredArgsConstructor
public class UserTypeController {

    private final UserTypeService userTypeService;

    @Operation(summary = "유형검사 시행")
    @PostMapping
    public CommonResponse<UserTypeAssignResponse> assignUserType(

            /* STEP 1 – 오늘 컨디션 */
            @Parameter(description = "STEP 1 – 오늘 컨디션",
                    schema = @Schema(implementation = Step1Question.class, enumAsRef = true))
            @RequestParam Step1Question step1Question,

            /* STEP 2 – 콘텐츠 선택 기준 */
            @Parameter(description = "STEP 2 – 콘텐츠 선택 기준",
                    schema = @Schema(implementation = Step2Question.class, enumAsRef = true))
            @RequestParam Step2Question step2Question,

            /* STEP 3 – 선호 콘텐츠 유형 */
            @Parameter(description = "STEP 3 – 선호 콘텐츠 유형",
                    schema = @Schema(implementation = ContentCategory.class, enumAsRef = true))
            @RequestParam ContentCategory favoriteCategory) {

            UserTypeAssignRequest request = UserTypeAssignRequest.builder()
                .step1Question(step1Question)
                .step2Question(step2Question)
                .favoriteCategory(favoriteCategory)
                .build();

        UserTypeAssignResponse response = userTypeService.assignCurrentUser(request);
        return CommonResponse.of(ResultCode.OK, response);
    }

    @Operation(summary = "유형검사 결과 조회")
    @GetMapping("/result")
    public CommonResponse<UserTypeResultDTO> getUserTypeResult() {
        return CommonResponse.of(ResultCode.OK, userTypeService.getCurrentUserType());
    }
}
