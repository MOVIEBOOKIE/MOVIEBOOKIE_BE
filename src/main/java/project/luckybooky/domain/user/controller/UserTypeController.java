package project.luckybooky.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.luckybooky.domain.user.dto.request.UserTypeAssignRequest;
import project.luckybooky.domain.user.dto.response.UserTypeResultDTO;
import project.luckybooky.domain.user.service.UserTypeService;
import project.luckybooky.global.apiPayload.response.CommonResponse;
import project.luckybooky.global.apiPayload.response.ResultCode;

@Tag(name = "User", description = "사용자 유형 검사 API")
@RestController
@RequestMapping("/api/user-type")
@RequiredArgsConstructor
public class UserTypeController {

    private final UserTypeService userTypeService;

    @Operation(summary = "유형검사 시행", description = "STEP1~STEP3 답변을 통해 사용자 유형을 판별하고 저장합니다.")
    @PostMapping
    public CommonResponse<UserTypeResultDTO> assignUserType(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "유형 검사 요청 정보",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UserTypeAssignRequest.class)
                    )
            )
            @RequestBody UserTypeAssignRequest request) {

        UserTypeResultDTO result = userTypeService.assignCurrentUser(request);
        return CommonResponse.of(ResultCode.OK, result);
    }


    @Operation(summary = "유형검사 결과 조회")
    @GetMapping("/result")
    public CommonResponse<UserTypeResultDTO> getUserTypeResult() {
        return CommonResponse.of(ResultCode.OK, userTypeService.getCurrentUserType());
    }
}
