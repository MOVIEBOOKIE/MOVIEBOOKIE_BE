package project.luckybooky.global.oauth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import project.luckybooky.global.jwt.JwtUtil;
import project.luckybooky.global.oauth.util.CookieUtil;

@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        String referer = request.getHeader("Referer");
        boolean isLocal = (referer != null && referer.contains("localhost:3000"));

        String accessToken = jwtUtil.createAccessToken(authentication.getName());
        String refreshToken = jwtUtil.createRefreshToken(authentication.getName());

        // 쿠키 설정 (배포환경에 따라 Secure 및 SameSite 설정)
        CookieUtil.addCookie(response, "accessToken", accessToken, jwtUtil.getAccessTokenValidity(), isLocal);
        CookieUtil.addCookie(response, "refreshToken", refreshToken, jwtUtil.getRefreshTokenValidity(), isLocal);

        String redirectUrl = isLocal ? "http://localhost:3000" : "https://movie-bookie.shop";
        log.info("🔹 로그인 성공! {} 환경으로 리디렉트: {}", isLocal ? "로컬" : "배포", redirectUrl);

        getRedirectStrategy().sendRedirect(request, response, redirectUrl + "/agreement");
    }
}
