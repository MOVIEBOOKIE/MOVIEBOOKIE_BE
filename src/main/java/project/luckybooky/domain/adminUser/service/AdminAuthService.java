package project.luckybooky.domain.adminUser.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.luckybooky.domain.adminUser.dto.AdminLoginRequest;
import project.luckybooky.domain.adminUser.dto.AdminLoginResponse;
import project.luckybooky.domain.adminUser.entity.AdminUser;
import project.luckybooky.domain.adminUser.repository.AdminUserRepository;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;
import project.luckybooky.global.jwt.JwtUtil;
import project.luckybooky.global.jwt.TokenService;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminUserRepository adminUserRepository;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;

    @Transactional
    public AdminLoginResponse login(AdminLoginRequest request) {
        AdminUser adminUser = adminUserRepository.findByName(request.getName())
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));

        if (!adminUser.getPassword().equals(request.getPassword())) {
            throw new BusinessException(ErrorCode.ADMIN_INVALID_CREDENTIALS);
        }

        String accessToken = jwtUtil.createAccessToken(adminUser.getName());
        String refreshToken = jwtUtil.createRefreshToken(adminUser.getName());

        long refreshTokenTtl = jwtUtil.getRemainingSeconds(refreshToken);
        tokenService.storeAdminRefreshToken(adminUser.getId(), refreshToken, refreshTokenTtl);

        adminUser.updateTokens(accessToken, refreshToken);
        adminUser.markLastLogin(LocalDateTime.now());
        adminUserRepository.save(adminUser);

        return AdminLoginResponse.builder()
                .adminId(adminUser.getId())
                .name(adminUser.getName())
                .role(adminUser.getRole())
                .build();
    }
}
