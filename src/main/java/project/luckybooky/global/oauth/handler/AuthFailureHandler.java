package project.luckybooky.global.oauth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import project.luckybooky.global.apiPayload.common.BaseResponse;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;

@Component
@RequiredArgsConstructor
public class AuthFailureHandler implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    private static final ErrorCode ERROR = ErrorCode.JWT_EXPIRED_TOKEN;

    @Value("${app.home-url:https://movie-bookie.shop}")
    private String homeUrl;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        String requestURI = request.getRequestURI();

        if (requestURI.matches("/events/\\d+/participants")) {
            String loginUrl = homeUrl + "/login";

            String host = request.getHeader("Host");
            if (host != null && (host.contains("localhost") || host.contains("127.0.0.1"))) {
                loginUrl = "http://localhost:3000/login";
            } else if (host != null && host.contains("dev-movie-bookie.shop/login")) {
                loginUrl = "https://dev-movie-bookie.shop/login";
            } else {
                loginUrl = "https://movie-bookie.shop/login";
            }

            response.sendRedirect(loginUrl);
            return;
        }

        response.setStatus(ERROR.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        BaseResponse<String> body = new BaseResponse<>(
                false,
                ERROR.getCode(),
                ERROR.getMessage()
        );

        objectMapper.writeValue(response.getWriter(), body);
    }
}