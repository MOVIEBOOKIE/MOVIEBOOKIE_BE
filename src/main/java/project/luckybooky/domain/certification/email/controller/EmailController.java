package project.luckybooky.domain.certification.email.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.luckybooky.domain.certification.email.dto.request.EmailRequestDTO;
import project.luckybooky.domain.certification.email.dto.request.EmailVerifyRequestDTO;
import project.luckybooky.domain.certification.email.service.EmailService;
import project.luckybooky.global.apiPayload.response.CommonResponse;
import project.luckybooky.global.apiPayload.response.ResultCode;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    /** 인증번호 발송 */
    @PostMapping("/send")
    public CommonResponse<Void> send(@Valid @RequestBody EmailRequestDTO dto) {
        emailService.sendCode(dto);
        return CommonResponse.ok(ResultCode.OK);
    }

    /** 인증번호 검증 */
    @PostMapping("/verify")
    public CommonResponse<Void> verify(@Valid @RequestBody EmailVerifyRequestDTO dto) {
        emailService.verify(dto);
        return CommonResponse.ok(ResultCode.OK);
    }
}

