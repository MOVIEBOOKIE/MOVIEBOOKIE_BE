package project.luckybooky.global.oauth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.repository.UserRepository;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;
import project.luckybooky.global.jwt.JwtUtil;

@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // 로그인 환경 판단
        String referer = request.getHeader("Referer");
        boolean isLocal = (referer != null && referer.contains("localhost:3000"));

        // 토큰 생성
        String username = authentication.getName();
        String accessToken = jwtUtil.createAccessToken(authentication.getName());
        String refreshToken = jwtUtil.createRefreshToken(authentication.getName());

        // 1) AccessToken 쿠키
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(!isLocal)
                .sameSite(isLocal ? "Lax" : "None")
                .path("/")
                .maxAge(jwtUtil.getAccessTokenValidity() / 1000)
                .build();

        // 2) RefreshToken 쿠키
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(!isLocal)
                .sameSite(isLocal ? "Lax" : "None")
                .path("/")
                .maxAge(jwtUtil.getRefreshTokenValidity() / 1000)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getId() != null) {
            log.info("🔹 [OAuth2 Login] 기존 토큰 정리 시작. userId={}", user.getId());
        }

        boolean firstLogin = (user.getUserType() == null);

        String baseUrl;
        String host = request.getHeader("Host");
        if (isLocal) {
            baseUrl = "http://localhost:3000";
        } else if (host != null && host.contains("dev-movie-bookie.shop")) {
            baseUrl = "https://dev-movie-bookie.shop";
        } else {
            baseUrl = "https://movie-bookie.shop";
        }
        String targetPath = firstLogin ? "/agreement" : "/";

        String redirectUrl = baseUrl + targetPath;
        log.info("🔹 로그인 성공! {} 환경, firstLogin={} → 리디렉트: {}", isLocal ? "로컬" : "배포", firstLogin, redirectUrl);

        log.info("[OAuth2] 로그인 성공 (신규유저={}): {}", firstLogin, redirectUrl);
        getRedirectStrategy().sendRedirect(request, response, baseUrl + targetPath);
    }
}
