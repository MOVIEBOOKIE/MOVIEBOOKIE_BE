package project.luckybooky.global.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import project.luckybooky.domain.user.dto.response.UserResponseDTO;
import project.luckybooky.domain.user.service.AuthService;
import project.luckybooky.domain.user.util.AuthenticatedUserUtils;

@Component
@RequiredArgsConstructor
public class UserContextService {
    private final AuthService authService;

    public Long getUserId() {
        String userEmail = AuthenticatedUserUtils.getAuthenticatedUserEmail();
        UserResponseDTO.AllInfoDTO userInfo = authService.getUserInfo(userEmail);
        return userInfo.getId();
    }
}
