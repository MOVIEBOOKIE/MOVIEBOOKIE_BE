package project.luckybooky.global.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import project.luckybooky.domain.participation.service.MailLinkTokenService;
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

        final String rawEventId = uriVars.get("eventId");
        final long pathEventId;
        try {
            pathEventId = Long.parseLong(rawEventId);
        } catch (NumberFormatException ex) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        String mt = req.getParameter("mt");
        if (mt == null || mt.isBlank()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        tokenService.validateOrNotFound(mt, pathEventId);
        return true;
    }
}
