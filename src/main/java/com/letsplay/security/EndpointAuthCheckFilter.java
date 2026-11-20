package com.letsplay.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class EndpointAuthCheckFilter extends OncePerRequestFilter {

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (isPublicRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean pathExists = doesPathExist(request);

        // Ila path makaynch ga3, khllih ywssel l DispatcherServlet bach y3ti 404
        if (!pathExists) {
            filterChain.doFilter(request, response);
            return;
        }

        // Path kayn: checki authentication (401 9bal 405 wla controller)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            writeUnauthorized(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean doesPathExist(HttpServletRequest request) {
        try {
            HandlerExecutionChain chain = requestMappingHandlerMapping.getHandler(request);
            return chain != null;
        } catch (HttpRequestMethodNotSupportedException ex) {
            // Path kayn walakin method machi m'supporté
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isPublicRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if (path.startsWith("/api/auth/")) {
            return true;
        }
        if (HttpMethod.GET.matches(method) && (path.equals("/products") || path.startsWith("/products/"))) {
            return true;
        }
        return false;
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "Unauthorized");
        body.put("message", "Authentication required");

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}