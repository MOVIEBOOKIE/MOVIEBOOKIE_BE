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
import project.luckybooky.global.jwt.JwtAuthenticationFilter;
import project.luckybooky.global.jwt.JwtUtil;
import project.luckybooky.global.oauth.handler.AuthFailureHandler;
import project.luckybooky.global.oauth.handler.OAuth2LoginSuccessHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final AuthFailureHandler authFailureHandler;  // 인증 실패 처리기

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1) CORS 설정
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration cfg = new CorsConfiguration();
                    cfg.addAllowedOriginPattern("*");
                    cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    cfg.setAllowedHeaders(List.of("*"));
                    cfg.setAllowCredentials(true);
                    cfg.setExposedHeaders(List.of("Authorization", "Set-Cookie"));
                    return cfg;
                }))

                // 2) CSRF, 세션 무상태(Stateless) 처리
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 3) 인증·인가 실패 핸들러
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authFailureHandler)
                )
                // 4) 공개 API
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/actuator/prometheus",
                                "/actuator/metrics",
                                "/"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/events/anonymous/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/email/send").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/test/users/tokens/**").permitAll()
                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/events/anonymous/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/login/kakao").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/reissue").permitAll()
                        .requestMatchers("/events/*/participants", "/events/*/participants/link").permitAll()
                        .requestMatchers(
                                "/index.html",
                                "/favicon.ico",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/static/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                // 5) OAuth2 로그인 핸들러
                .oauth2Login(oauth2 ->
                        oauth2.successHandler(oAuth2LoginSuccessHandler)
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}