package project.luckybooky.domain.adminUser.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import project.luckybooky.domain.adminUser.entity.AdminUser;
import project.luckybooky.domain.adminUser.repository.AdminUserRepository;
import project.luckybooky.domain.user.util.AuthenticatedUserUtils;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@Component
@RequiredArgsConstructor
public class AdminContextService {

    private final AdminUserRepository adminUserRepository;

    public AdminUser getCurrentAdminUser() {
        String adminName = AuthenticatedUserUtils.getAuthenticatedUserEmail();
        return adminUserRepository.findByName(adminName)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));
    }
}
