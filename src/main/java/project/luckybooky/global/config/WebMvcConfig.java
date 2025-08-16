package project.luckybooky.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import project.luckybooky.global.security.MailLinkTokenInterceptor;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final MailLinkTokenInterceptor mailLinkTokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(mailLinkTokenInterceptor)
                .addPathPatterns("/api/events/{eventId:\\d+}/participants/**");
    }
}