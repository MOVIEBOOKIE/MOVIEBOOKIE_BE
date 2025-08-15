package project.luckybooky.global.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import project.luckybooky.domain.notification.service.MailLinkTokenService;
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
        // pathVariable에서 eventId 구함
        Map<String, String> uriVars =
                (Map<String, String>) req.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (uriVars == null || !uriVars.containsKey("eventId")) {
            return true; // 다른 엔드포인트
        }

        Long pathEventId = Long.valueOf(uriVars.get("eventId"));
        String mt = req.getParameter("mt");

        if (mt == null || mt.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        tokenService.validateOrThrow(mt, pathEventId);
        return true;
    }
}
