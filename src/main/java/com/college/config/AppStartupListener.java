package com.college.config;

import com.college.util.HibernateUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppStartupListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(AppStartupListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Initializing application...");
        ThymeleafConfig.init(sce.getServletContext());
        logger.info("Application initialized successfully.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Shutting down application...");
        HibernateUtil.shutdown();
    }
}
