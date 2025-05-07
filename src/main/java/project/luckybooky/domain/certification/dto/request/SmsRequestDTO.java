package project.luckybooky.domain.certification.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SmsRequestDTO {
    @NotEmpty(message = "휴대폰 번호를 입력해주세요")
    private String phoneNum;
}
