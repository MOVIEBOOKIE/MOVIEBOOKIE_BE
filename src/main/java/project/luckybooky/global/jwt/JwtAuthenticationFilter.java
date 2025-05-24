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

    // 예외 처리할 API 리스트 (스웨거 및 로그인 관련 요청 제외)
    private static final List<String> EXCLUDED_URLS = List.of(
            "/api/auth/login/kakao", "/swagger-ui", "/v3/api-docs", "/swagger-resources"
    );

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        log.info("[JWT 필터] 요청 URI: {}", requestURI);

        // 1) Swagger/UI나 로그인 관련 요청은 통과
        if (isExcluded(requestURI)) {
            log.info("[JWT 필터] 인증 제외 URL, 그대로 통과: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // ─────────────────────────────────────────────────────────
        // ★ 디버깅용: 요청에 담긴 모든 쿠키 로그로 찍기 ★
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                log.debug("[JWT 필터] Request Cookie: {}={}", cookie.getName(), cookie.getValue());
            }
        } else {
            log.debug("[JWT 필터] Request 에 Cookie 자체가 없음");
        }
        // ─────────────────────────────────────────────────────────

        // 2) 헤더 혹은 쿠키에서 토큰 꺼내기
        String token = resolveToken(request);
        log.info("[JWT 필터] 추출한 토큰: {}", token == null ? "없음" : token);

        // 3) 토큰이 없거나 유효하지 않으면 즉시 401
        if (token == null) {
            log.warn("[JWT 필터] 토큰 없음 → 401 응답");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증이 필요합니다.");
            return;
        }
        try {
            if (!jwtUtil.validateToken(token)) {
                log.warn("[JWT 필터] 토큰 유효성 검사 실패 → 401 응답");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증이 필요합니다.");
                return;
            }
        } catch (Exception ex) {
            log.warn("[JWT 필터] 토큰 검증 중 예외 발생: {}", ex.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증이 필요합니다.");
            return;
        }

        // 4) 토큰이 유효하면 SecurityContext에 Authentication 설정
        String email = jwtUtil.extractEmail(token);
        log.info("[JWT 필터] 토큰 유효, email={}", email);

        JwtAuthenticationToken authentication = new JwtAuthenticationToken(email);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("[JWT 필터] SecurityContext에 Authentication 저장: {}",
                SecurityContextHolder.getContext().getAuthentication());

        // 5) (선택) downstream에서 Authorization 헤더가 필요하면 첨부
        if (request.getHeader("Authorization") == null) {
            request.setAttribute("Authorization", "Bearer " + token);
            log.debug("[JWT 필터] request attribute에 Authorization 헤더 추가");
        }

        // 6) 다음 필터/컨트롤러로 진행
        filterChain.doFilter(request, response);
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
