package tech.cspioneer.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tech.cspioneer.backend.common.ApiResponse;

import java.io.IOException;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(RestAccessDeniedHandler.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            String user = (auth == null) ? "anonymous" : String.valueOf(auth.getPrincipal());
            String authz = (auth == null || auth.getAuthorities() == null) ? "" : auth.getAuthorities().toString();
            log.warn("AccessDenied(filter) path={} method={} userUuid={} authz={} msg={} requestId={}",
                    request.getRequestURI(), request.getMethod(), user, authz, accessDeniedException.getMessage(), MDC.get("requestId"));
        } catch (Exception ignore) {}
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Void> body = ApiResponse.error(403, "无权限");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
