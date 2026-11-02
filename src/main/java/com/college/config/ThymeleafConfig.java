package com.college.config;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

public class ThymeleafConfig {

    private static JakartaServletWebApplication application;
    private static TemplateEngine templateEngine;

    public static synchronized void init(ServletContext servletContext) {
        if (templateEngine != null) return;

        application = JakartaServletWebApplication.buildApplication(servletContext);

        WebApplicationTemplateResolver resolver = new WebApplicationTemplateResolver(application);
        resolver.setPrefix("/WEB-INF/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);
        templateEngine.addDialect(new FieldAttributeDialect());
    }

    public static TemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    public static void render(HttpServletRequest request, HttpServletResponse response,
                              String templateName, Map<String, Object> variables) throws IOException {

        if (application == null) init(request.getServletContext());

        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        IWebExchange webExchange = application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange, webExchange.getLocale());

        context.setVariable("contextPath", request.getContextPath());

        Enumeration<String> attrNames = request.getAttributeNames();
        while (attrNames.hasMoreElements()) {
            String name = attrNames.nextElement();
            context.setVariable(name, request.getAttribute(name));
        }

        if (variables != null) {
            variables.forEach(context::setVariable);
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            context.setVariable("currentUserId", session.getAttribute("userId"));
            context.setVariable("currentUserName", session.getAttribute("userName"));
            context.setVariable("currentUserRole", session.getAttribute("userRole"));
            context.setVariable("currentUserEmail", session.getAttribute("userEmail"));
        }

        if (!context.containsVariable("successMessage") && request.getParameter("success") != null) {
            context.setVariable("successMessage", request.getParameter("success"));
        }
        if (!context.containsVariable("errorMessage") && request.getParameter("error") != null) {
            context.setVariable("errorMessage", request.getParameter("error"));
        }
        if (!context.containsVariable("infoMessage") && request.getParameter("info") != null) {
            context.setVariable("infoMessage", request.getParameter("info"));
        }

        templateEngine.process(templateName, context, response.getWriter());
    }
}
