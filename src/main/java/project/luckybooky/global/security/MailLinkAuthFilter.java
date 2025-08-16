package project.luckybooky.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import project.luckybooky.domain.participation.service.MailLinkTokenService;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@Component
@RequiredArgsConstructor
public class MailLinkAuthFilter extends OncePerRequestFilter {

    private static final Pattern PATH =
            Pattern.compile("^/api/(?:v\\d+/)?events/([^/]+)/participants(?:/.*)?$");

    private final MailLinkTokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String uri = req.getRequestURI();
        var m = PATH.matcher(uri);
        if (!m.matches()) {
            chain.doFilter(req, res);
            return;
        }

        Long pathEventId = Long.valueOf(m.group(1));
        String mt = req.getParameter("mt");

        if (mt == null || mt.isBlank()) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        tokenService.validateOrNotFound(mt, pathEventId);

        AbstractAuthenticationToken auth = new AbstractAuthenticationToken(
                List.of(new SimpleGrantedAuthority("ROLE_MAIL_LINK"))) {
            @Override
            public Object getCredentials() {
                return "mt";
            }

            @Override
            public Object getPrincipal() {
                return "mail-link:event:" + pathEventId;
            }
        };
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);

        chain.doFilter(req, res);
    }
}
