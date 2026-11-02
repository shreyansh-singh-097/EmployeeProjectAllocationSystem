package com.college.controller;

import com.college.config.ThymeleafConfig;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ErrorServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        renderError(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        renderError(request, response);
    }

    private void renderError(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Map<String, Object> model = new HashMap<>();

        Object customMsg = request.getAttribute("errorMessage");
        Object customCode = request.getAttribute("errorCode");

        Integer containerCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String containerMsg = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Throwable ex = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        int code = customCode != null ? (Integer) customCode
                : containerCode != null ? containerCode : 500;
        String message = customMsg != null ? customMsg.toString()
                : containerMsg != null && !containerMsg.isBlank() ? containerMsg
                : ex != null ? ex.getMessage()
                : "An unexpected error occurred.";

        model.put("errorCode", code);
        model.put("errorMessage", message);

        response.setStatus(code);
        ThymeleafConfig.render(request, response, "error", model);
    }
}
