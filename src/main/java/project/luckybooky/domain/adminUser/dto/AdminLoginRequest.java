package project.luckybooky.domain.adminUser.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String password;
}
