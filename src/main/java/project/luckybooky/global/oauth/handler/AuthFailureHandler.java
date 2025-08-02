package project.luckybooky.global.oauth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
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

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
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