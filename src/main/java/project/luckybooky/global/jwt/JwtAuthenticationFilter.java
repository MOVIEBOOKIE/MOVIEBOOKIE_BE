package project.luckybooky.global.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import project.luckybooky.global.oauth.util.CookieUtil;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private static final List<String> EXCLUDED_URLS = List.of(
            "/api/auth/login/kakao", "/swagger-ui", "/v3/api-docs", "/swagger-resources", "/api/events/anonymous",
            "/api/test/users/tokens", "api/test/events/", "/actuator/prometheus", "/actuator/metrics", "/api/email/send"
    );

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        if (EXCLUDED_URLS.stream().anyMatch(uri::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 디버깅: 쿠키가 정말로 들어오는지
        if (request.getCookies() != null) {
            for (var c : request.getCookies()) {
                log.debug("[JWT 필터] Cookie: {}={}", c.getName(), c.getValue());
            }
        }

        // 헤더 우선, 없으면 쿠키에서
        String bearer = request.getHeader("Authorization");
        String token = bearer != null && bearer.startsWith("Bearer ")
                ? bearer.substring(7)
                : CookieUtil.getCookieValue(request, "accessToken");

        log.info("[JWT 필터] 추출한 토큰: {}", token == null ? "없음" : token);

        if (token != null && jwtUtil.validateToken(token)) {
            String email = jwtUtil.extractEmail(token);
            SecurityContextHolder.getContext()
                    .setAuthentication(new JwtAuthenticationToken(email));
            log.info("[JWT 필터] 인증 성공: {}", email);
            filterChain.doFilter(request, response);
        } else {
            log.warn("[JWT 필터] 인증 실패 → 401");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증이 필요합니다.");
        }
    }

    private boolean isExcluded(String requestURI) {
        return EXCLUDED_URLS.stream().anyMatch(requestURI::startsWith);
    }

    private String resolveToken(HttpServletRequest request) {
        // 우선적으로 Authorization 헤더에서 가져옴
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        // 헤더에 없으면 쿠키에서 accessToken 가져옴
        return CookieUtil.getCookieValue(request, "accessToken");
    }
}
