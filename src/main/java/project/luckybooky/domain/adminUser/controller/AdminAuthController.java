package project.luckybooky.domain.adminUser.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.luckybooky.domain.adminUser.dto.AdminLoginRequest;
import project.luckybooky.domain.adminUser.dto.AdminLoginResponse;
import project.luckybooky.domain.adminUser.service.AdminAuthService;
import project.luckybooky.global.apiPayload.response.CommonResponse;
import project.luckybooky.global.apiPayload.response.ResultCode;

@Tag(name = "AdminAuth", description = "어드민 인증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @Operation(summary = "어드민 로그인", description = "수동으로 등록된 어드민 계정 이메일/비밀번호로 로그인합니다.")
    @PostMapping("/login")
    public CommonResponse<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return CommonResponse.of(ResultCode.ADMIN_LOGIN_OK, adminAuthService.login(request));
    }
}

