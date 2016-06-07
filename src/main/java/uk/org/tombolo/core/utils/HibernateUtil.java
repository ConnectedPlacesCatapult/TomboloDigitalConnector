package uk.org.tombolo.core.utils;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.Properties;
import java.util.ServiceConfigurationError;
import java.util.function.Consumer;
import java.util.function.Function;

public class HibernateUtil {
	private static SessionFactory sessionFactory;
    private static Session sharedSession;

    public static void startup() {
        sessionFactory = buildSessionFactory();
        sharedSession = sessionFactory.openSession();
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
        cfg.addProperties(getDatabaseConnectionProperties());

        if (null != System.getenv("DATABASE_URI")) {
            cfg.setProperty("hibernate.connection.url", System.getenv("DATABASE_URI"));
        }

        try {
            return cfg.buildSessionFactory();
        } catch (Exception ex) {
            throw new ServiceConfigurationError("Failed to create SessionFactory", ex);
        }
    }

    private static Properties getDatabaseConnectionProperties() {
        try {
            Properties dbConnectionProperties = new Properties();
            dbConnectionProperties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("DBConnection.properties"));
            return dbConnectionProperties;
        } catch (Exception ex) {
            throw new ServiceConfigurationError("Failed to load database connection configuration from DBConnection.properties. See README for setup instructions.", ex);
        }
    }

    private static boolean isStarted() {
        return null != sharedSession;
    }
}
