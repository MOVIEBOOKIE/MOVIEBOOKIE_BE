package project.luckybooky.domain.certification.email.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailRequestDTO {

    @Email(message = "유효한 이메일을 입력해주세요")
    private String email;
}
