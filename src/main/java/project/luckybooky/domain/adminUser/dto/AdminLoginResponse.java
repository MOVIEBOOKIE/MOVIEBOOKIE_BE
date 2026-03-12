package project.luckybooky.domain.adminUser.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import project.luckybooky.domain.adminUser.entity.AdminRole;

@Getter
@Builder
@AllArgsConstructor
public class AdminLoginResponse {
    private Long adminId;
    private String name;
    private AdminRole role;
}
