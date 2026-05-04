package project.luckybooky.global.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class InstanceIdHeaderFilter extends OncePerRequestFilter {

    private static final String HEADER_NAME = "X-Instance-Id";

    @Value("${app.instance-id:}")
    private String configuredInstanceId;

    @Value("${server.port:}")
    private String configuredPort;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        response.setHeader(HEADER_NAME, resolveInstanceId(request));
        filterChain.doFilter(request, response);
    }

    private String resolveInstanceId(HttpServletRequest request) {
        if (configuredInstanceId != null && !configuredInstanceId.isBlank()) {
            return configuredInstanceId;
        }
        if (configuredPort != null && !configuredPort.isBlank()) {
            return "port-" + configuredPort;
        }
        return "port-" + request.getLocalPort();
    }
}
