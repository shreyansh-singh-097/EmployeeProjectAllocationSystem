package com.college.util;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class HibernateUtil {

    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);
    private static SessionFactory sessionFactory;

    static {
        try {
            StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();
            registryBuilder.configure("hibernate.cfg.xml");

            Map<String, Object> overrideSettings = new HashMap<>();

            String dbUrl = System.getenv("DB_URL");
            if (dbUrl == null || dbUrl.isBlank()) {
                dbUrl = System.getProperty("db.url");
            }
            if (dbUrl != null && !dbUrl.isBlank()) {
                overrideSettings.put("hibernate.connection.url", dbUrl);
            }

            String dbUser = System.getenv("DB_USER");
            if (dbUser == null || dbUser.isBlank()) {
                dbUser = System.getProperty("db.user");
            }
            if (dbUser != null && !dbUser.isBlank()) {
                overrideSettings.put("hibernate.connection.username", dbUser);
            }

            String dbPassword = System.getenv("DB_PASSWORD");
            if (dbPassword == null) {
                dbPassword = System.getProperty("db.password");
            }
            if (dbPassword != null) {
                overrideSettings.put("hibernate.connection.password", dbPassword);
            }

            if (!overrideSettings.isEmpty()) {
                registryBuilder.applySettings(overrideSettings);
            }

            StandardServiceRegistry standardRegistry = registryBuilder.build();
            MetadataSources sources = new MetadataSources(standardRegistry);
            Metadata metadata = sources.getMetadataBuilder().build();

            sessionFactory = metadata.getSessionFactoryBuilder().build();
            logger.info("Hibernate SessionFactory initialized.");

        } catch (Throwable ex) {
            logger.error("SessionFactory creation failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }
}
