package project.luckybooky.domain.user.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;
import project.luckybooky.global.jwt.JwtAuthenticationToken;

@Component
public class AuthenticatedUserUtils {

    /**
     * 현재 인증된 사용자 이메일을 가져오는 메서드
     */
    public static String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken) {
            return (String) authentication.getPrincipal();
        }

        throw new BusinessException(ErrorCode.UNAUTHORIZED);    }
}
