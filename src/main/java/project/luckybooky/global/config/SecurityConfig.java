package project.luckybooky.global.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import project.luckybooky.domain.user.repository.UserRepository;
import project.luckybooky.global.jwt.JwtAuthenticationFilter;
import project.luckybooky.global.jwt.JwtUtil;
import project.luckybooky.global.oauth.handler.AuthFailureHandler;
import project.luckybooky.global.oauth.handler.OAuth2LoginSuccessHandler;
import project.luckybooky.global.security.MailLinkAuthFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final AuthFailureHandler authFailureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            MailLinkAuthFilter mailLinkAuthFilter,
            UserRepository userRepository
    ) throws Exception {

        http
                // 1) CORS
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration cfg = new CorsConfiguration();
                    cfg.addAllowedOriginPattern("*");
                    cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    cfg.setAllowedHeaders(List.of("*"));
                    cfg.setAllowCredentials(true);
                    cfg.setExposedHeaders(List.of("Authorization", "Set-Cookie"));
                    return cfg;
                }))

                // 2) CSRF 비활성 + Stateless
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3) 필터 순서: MailLinkAuth → JwtAuth → UsernamePassword
                .addFilterBefore(mailLinkAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new JwtAuthenticationFilter(jwtUtil), MailLinkAuthFilter.class)

                // 4) 인가 규칙
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/api/**/participants/**").authenticated()

                        // 공개 라우트들
                        .requestMatchers(
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                                "/swagger-resources/**", "/webjars/**",
                                "/", "/index.html", "/static/**", "/favicon.ico",
                                "/css/**", "/js/**", "/images/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/events/anonymous/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/email/send").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/test/users/tokens/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/login/kakao").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/reissue").permitAll()

                        // 프론트 라우트(HTML)는 서버에서 열어둡니다 (Nginx/게이트에서 mt 체크 가능)
                        .requestMatchers(HttpMethod.GET, "/events/*/participants/**").permitAll()

                        // 나머지는 필요 정책에 맞게 — 보통 permitAll 또는 authenticated
                        .anyRequest().permitAll()
                )

                // 5) 인증 실패 핸들러
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authFailureHandler))

                // 6) OAuth2 로그인
                .oauth2Login(oauth2 -> oauth2.successHandler(oAuth2LoginSuccessHandler));

        return http.build();
    }
}
