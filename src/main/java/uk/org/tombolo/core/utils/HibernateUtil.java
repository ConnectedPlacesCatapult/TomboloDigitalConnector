package uk.org.tombolo.core.utils;

import org.hibernate.CacheMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.ServiceConfigurationError;
import java.util.function.Consumer;
import java.util.function.Function;

public class HibernateUtil {
	private static SessionFactory sessionFactory;
    private static Session sharedSession;

    public static void startup() {
        sessionFactory = buildSessionFactory();
        sharedSession = sessionFactory.openSession();
        sharedSession.setCacheMode(CacheMode.NORMAL);
    }
    
    public static void shutdown() {
    	// Close caches and connection pools
        sharedSession.close();
    	sessionFactory.close();
    }

    public static void restart() {
        if (isStarted()) { shutdown(); }
        startup();
    }

    public static <T> T withSession(Function<Session, T> fn) {
        return fn.apply(sharedSession);
    }

    public static void withSession(Consumer<Session> fn) {
        fn.accept(sharedSession);
    }

    private static SessionFactory buildSessionFactory() {
        // Create the SessionFactory from hibernate.cfg.xml
        Configuration cfg = new Configuration().configure();
        applyEnvironmentVariables(cfg);

        try {
            return cfg.buildSessionFactory();
        } catch (Exception ex) {
            throw new ServiceConfigurationError("Failed to create SessionFactory", ex);
        }
    }

    private static void applyEnvironmentVariables(Configuration cfg) {
        if (null != System.getProperty("databaseURI")) {
            cfg.setProperty("hibernate.connection.url", System.getProperty("databaseURI"));
        }

        if (null != System.getProperty("databaseUsername")) {
            cfg.setProperty("hibernate.connection.username", System.getProperty("databaseUsername"));
        }

        if (null != System.getProperty("databasePassword")) {
            cfg.setProperty("hibernate.connection.password", System.getProperty("databasePassword"));
        }
    }

    private static boolean isStarted() {
        return null != sharedSession;
    }
}
