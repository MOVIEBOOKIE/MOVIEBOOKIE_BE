package project.luckybooky.domain.certification.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.luckybooky.domain.certification.dto.request.SmsRequestDTO;
import project.luckybooky.global.apiPayload.response.CommonResponse;
import project.luckybooky.global.apiPayload.response.ResultCode;

@RestController
@RequestMapping("/sms")
@RequiredArgsConstructor
public class SmsController {

    private final project.luckybooky.domain.certification.service.SmsService smsService;

    @PostMapping("/send")
    public CommonResponse<Void> sendSms(@Valid @RequestBody SmsRequestDTO dto) {
        smsService.sendCertificationCode(dto);
        return CommonResponse.ok(ResultCode.OK);
    }
}
