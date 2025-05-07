package project.luckybooky.domain.certification.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsVerifyRequestDTO {

    @NotEmpty(message = "휴대폰 전화번호를 입력해주세요")
    private String phoneNum;

    @NotEmpty(message = "인증번호를 입력해주세요")
    private String certificationCode;
}
