package io.github.giangggvu.shop.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;


@Component
class SecurityProblemHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final AuthenticationEntryPoint bearerEntryPoint = new BearerTokenAuthenticationEntryPoint();
    private final AccessDeniedHandler bearerAccessDenied = new BearerTokenAccessDeniedHandler();
    private final ObjectMapper json;

    SecurityProblemHandler(ObjectMapper json) {
        this.json = json;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
            throws IOException, ServletException {
        bearerEntryPoint.commence(request, response, ex);
        String detail = ex instanceof InvalidBearerTokenException
            ? "Your login token is invalid or has expired. Please log in again."
            : "Please log in to continue";
        write(request, response, HttpStatus.UNAUTHORIZED, "Unauthorized", detail);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
            throws IOException, ServletException {
        bearerAccessDenied.handle(request, response, ex);
        write(request, response, HttpStatus.FORBIDDEN, "Forbidden", "You do not have permission to do this");
    }

    private void write(HttpServletRequest request, HttpServletResponse response, HttpStatus status, String title,
            String detail) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "about:blank");
        body.put("title", title);
        body.put("status", status.value());
        body.put("detail", detail);
        body.put("instance", request.getRequestURI());
        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/problem+json");
        json.writeValue(response.getOutputStream(), body);
    }
}
