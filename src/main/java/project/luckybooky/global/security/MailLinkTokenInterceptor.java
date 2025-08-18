package project.luckybooky.global.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import project.luckybooky.domain.secureMail.service.MailLinkTokenService;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailLinkTokenInterceptor implements HandlerInterceptor {

    private final MailLinkTokenService tokenService;

    @Override
    @SuppressWarnings("unchecked")
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        Map<String, String> uriVars =
                (Map<String, String>) req.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (uriVars == null || !uriVars.containsKey("eventId")) {
            return true;
        }

        long pathEventId = Long.parseLong(uriVars.get("eventId"));
        String et = req.getParameter("et");
        if (et == null || et.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 실제 접근 경로와 토큰의 path를 묶어 위변조/경로 전용화
        tokenService.validateOrThrow(et, pathEventId, req.getRequestURI());
        return true;
    }
}
