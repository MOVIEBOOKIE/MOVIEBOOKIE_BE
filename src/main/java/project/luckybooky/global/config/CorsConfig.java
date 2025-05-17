package project.luckybooky.global.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 도메인을 일일이 나열하거나 와일드카드 패턴을 사용할 수 있습니다.
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",
                "https://api.movie-bookie.shop",
                "https://movie-bookie.shop",
                "https://*.movie-bookie.shop"     // www 도메인 포함
        ));

        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // 모든 헤더 허용
        config.setAllowedHeaders(List.of("*"));

        // 클라이언트에서 읽어야 하는 헤더
        config.setExposedHeaders(List.of("Authorization", "Set-Cookie", "REFRESH_TOKEN"));

        // 쿠키 전달 허용
        config.setAllowCredentials(true);

        // 캐시 시간 (초)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 모든 경로에 대해 위 설정 적용
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
