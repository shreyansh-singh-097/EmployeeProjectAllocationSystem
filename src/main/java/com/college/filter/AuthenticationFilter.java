package com.college.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class AuthenticationFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("AuthenticationFilter initialized.");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String contextPath = httpRequest.getContextPath();
        String requestUri = httpRequest.getRequestURI();
        String path = requestUri.substring(contextPath.length());

        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        boolean isAuthenticated = (session != null && session.getAttribute("userId") != null);

        if (!isAuthenticated) {
            httpResponse.sendRedirect(contextPath + "/login?error=" + urlEncode("Please log in to continue."));
            return;
        }

        String userRole = (String) session.getAttribute("userRole");

        if (isAdminOnlyPath(path)) {
            if (!"ADMIN".equalsIgnoreCase(userRole)) {
                httpResponse.sendRedirect(contextPath + "/dashboard?error=" +
                        urlEncode("Access denied: Administrator privileges required."));
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        if (path == null || path.isEmpty() || path.equals("/")) {
            return true;
        }
        return path.startsWith("/login") ||
                path.startsWith("/logout") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/") ||
                path.equals("/favicon.ico");
    }

    private boolean isAdminOnlyPath(String path) {
        return path.startsWith("/employees") ||
                path.startsWith("/projects") ||
                path.startsWith("/allocations");
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    @Override
    public void destroy() {
    }
}
