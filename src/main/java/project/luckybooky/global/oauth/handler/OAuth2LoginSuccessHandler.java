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
import project.luckybooky.global.jwt.JwtUtil;

@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // 로그인 환경 판단
        String referer = request.getHeader("Referer");
        boolean isLocal = (referer != null && referer.contains("localhost:3000"));

        // 토큰 생성
        String accessToken = jwtUtil.createAccessToken(authentication.getName());
        String refreshToken = jwtUtil.createRefreshToken(authentication.getName());

        // 1) AccessToken 쿠키
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(!isLocal)                       // 로컬이면 false, 배포면 true
                .path("/")
                .maxAge(jwtUtil.getAccessTokenValidity() / 1000)
                .sameSite("None")                       // ★ 크로스사이트 허용
                .build();

        // 2) RefreshToken 쿠키
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(!isLocal)
                .path("/")
                .maxAge(jwtUtil.getRefreshTokenValidity() / 1000)
                .sameSite("None")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // 리다이렉트
        String redirectUrl = isLocal
                ? "http://localhost:3000/agreement"
                : "https://movie-bookie.shop/agreement";

        log.info("🔹 로그인 성공! {} 환경으로 리디렉트: {}", isLocal ? "로컬" : "배포", redirectUrl);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
