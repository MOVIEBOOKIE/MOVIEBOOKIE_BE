package project.luckybooky.global.config;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import project.luckybooky.global.security.MailLinkTokenInterceptor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final MailLinkTokenInterceptor linkInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(linkInterceptor)
                .addPathPatterns("/events/*/participants")
                .excludePathPatterns("/events/*/participants/link");

    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter jackson) {
                List<MediaType> types = new ArrayList<>(jackson.getSupportedMediaTypes());
                types.add(MediaType.TEXT_PLAIN);
                types.add(MediaType.APPLICATION_OCTET_STREAM);
                jackson.setSupportedMediaTypes(types);
            }
        }
    }
}
