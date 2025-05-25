package project.luckybooky.global.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import project.luckybooky.global.oauth.handler.OAuth2LoginSuccessHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserRepository userRepository)
            throws Exception {
        http
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration cfg = new CorsConfiguration();
                    cfg.addAllowedOriginPattern("*");
                    cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    cfg.setAllowedHeaders(List.of("*"));
                    cfg.setAllowCredentials(true);
                    cfg.setExposedHeaders(List.of("Authorization", "Cookie"));
                    return cfg;
                })).csrf(AbstractHttpConfigurer::disable)  // CSRF 비활성화
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/api/health").permitAll() // 인증 없이 접근 허용
                        .requestMatchers("/", "/index.html", "/static/**", "/favicon.ico").permitAll() // 정적 파일 허용
                        .requestMatchers("/**").permitAll()  // 모든 요청 허용 (테스트용)
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2LoginSuccessHandler) // OAuth2 로그인 성공 핸들러 추가
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}