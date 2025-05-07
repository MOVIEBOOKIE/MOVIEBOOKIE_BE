package project.luckybooky.domain.certification.email.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import retrofit2.http.GET;

@Getter
@Setter
public class EmailVerifyRequestDTO {

    @Email(message = "유효한 이메일을 입력해주세요")
    private String email;

    @NotEmpty(message = "인증번호를 입력해주세요")
    private String certificationCode;
}
