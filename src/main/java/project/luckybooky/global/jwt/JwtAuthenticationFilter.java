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

  // ✅ 로그인/공개 경로는 '아예' 필터 스킵
  private static final List<String> EXCLUDED_URLS = List.of(
      "/api/auth/login/kakao",
      "/dev/api/auth/login/kakao",
      "/v3/api-docs",
      "/swagger-resources",
      "/api/events/anonymous",
      "/api/test/users/tokens",
      "/api/test/events/",
      "/actuator/prometheus",
      "/actuator/metrics",
      "/api/email/send",
      "/api/auth/reissue",
      "/dev/swagger-ui",
      "/dev/v3/api-docs",
      "/dev/swagger-resources",
      "/events/",
      "/images/",
      "/css/",
      "/js/",
      "/static/",
      "/discord/interactions",
      "/api/admin/events"
  );

  public JwtAuthenticationFilter(JwtUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    final String uri = request.getRequestURI();

    if (isExcluded(uri)) {
      log.info("[JWT 필터] EXCLUDED URI → pass: {}", uri);
      filterChain.doFilter(request, response);
      return;
    }

    String token = resolveToken(request);
    log.info("[JWT 필터] URI: {}, token 존재: {}", uri, token != null);

    if (token != null && jwtUtil.validateToken(token)) {
      String email = jwtUtil.extractEmail(token);
      log.info("[JWT 필터] 토큰 검증 성공 - email: {}", email);
      SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(email));
    } else {
      SecurityContextHolder.clearContext();
      if (token != null) {
        log.info("[JWT 필터] 토큰 유효하지 않음 → 인증 미설정 상태로 통과");
      }
    }

    filterChain.doFilter(request, response);
  }

  private boolean isExcluded(String requestURI) {
    return EXCLUDED_URLS.stream().anyMatch(requestURI::startsWith);
  }

  private String resolveToken(HttpServletRequest request) {
    String bearer = request.getHeader("Authorization");
    if (bearer != null && bearer.startsWith("Bearer ")) {
      return bearer.substring(7);
    }
    return CookieUtil.getCookieValue(request, "accessToken");
  }
}
