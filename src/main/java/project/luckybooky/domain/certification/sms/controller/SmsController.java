package project.luckybooky.domain.certification.sms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.luckybooky.domain.certification.sms.Service.SmsService;
import project.luckybooky.domain.certification.sms.dto.request.SmsRequestDTO;
import project.luckybooky.domain.certification.sms.dto.request.SmsVerifyRequestDTO;
import project.luckybooky.domain.user.util.AuthenticatedUserUtils;
import project.luckybooky.global.apiPayload.response.CommonResponse;
import project.luckybooky.global.apiPayload.response.ResultCode;

@Tag(name = "Auth", description = "sms(문자)인증 API")
@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsService smsService;

    /**
     * 인증번호 발송
     **/
    @Operation(
            summary = "SMS 인증번호 발송",
            description = """
                    - body: `{"phoneNum": "01012345678"}`
                    - 응답: 200 OK, body { "code": "COMMON_200", "message": "성공" }
                    """
    )
    @PostMapping("/send")
    public CommonResponse<Void> sendSms(@Valid @RequestBody SmsRequestDTO dto) {
        smsService.sendCertificationCode(dto);
        return CommonResponse.ok(ResultCode.OK);
    }

    /**
     * 인증번호 검증
     **/
    @Operation(
            summary = "SMS 인증번호 검증",
            description = """
                    - body: `{"phoneNum": "01012345678", "certificationCode": "1234"}`
                    - 검증 성공 시 전화번호가 회원 DB에 저장
                    """
    )
    @PostMapping("/verify")
    public CommonResponse<Void> verifySms(
            @Valid @RequestBody SmsVerifyRequestDTO dto) {

        String loginEmail = AuthenticatedUserUtils.getAuthenticatedUserEmail();
        smsService.verifyCertificationCode(dto, loginEmail);
        return CommonResponse.ok(ResultCode.OK);
    }
}
