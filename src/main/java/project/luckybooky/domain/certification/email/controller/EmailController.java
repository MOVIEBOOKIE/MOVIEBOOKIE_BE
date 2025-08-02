package project.luckybooky.domain.certification.email.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.luckybooky.domain.certification.email.dto.request.EmailRequestDTO;
import project.luckybooky.domain.certification.email.dto.request.EmailVerifyRequestDTO;
import project.luckybooky.domain.certification.email.service.EmailService;
import project.luckybooky.domain.user.util.AuthenticatedUserUtils;
import project.luckybooky.global.apiPayload.response.CommonResponse;
import project.luckybooky.global.apiPayload.response.ResultCode;

@Tag(name = "Auth", description = "이메일 인증 API")
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    /**
     * 인증번호 발송
     */
    @Operation(
            summary = "이메일 인증번호 발송",
            description = """
                    - body: `{"email": "example@mail.com"}`
                    - 3분 동안 유효한 4자리 코드 발송
                    """
    )
    @PostMapping("/send")
    public CommonResponse<Void> send(@Valid @RequestBody EmailRequestDTO dto) {
        emailService.sendCode(dto);
        return CommonResponse.ok(ResultCode.OK);
    }

    /**
     * 인증번호 검증
     */
    @Operation(
            summary = "이메일 인증번호 검증",
            description = """
                    - body: `{"email": "example@mail.com", "certificationCode": "1234"}`
                    - 성공 시 사용자의 certificationEmail(인증된 이메일) 필드에 저장
                    """
    )
    @PostMapping("/verify")
    public CommonResponse<Void> verify(@Valid @RequestBody EmailVerifyRequestDTO dto) {
        String loginEmail = AuthenticatedUserUtils.getAuthenticatedUserEmail();
        emailService.verify(dto, loginEmail);
        return CommonResponse.ok(ResultCode.OK);
    }
}

