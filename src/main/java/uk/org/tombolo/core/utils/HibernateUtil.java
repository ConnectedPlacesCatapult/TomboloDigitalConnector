package uk.org.tombolo.core.utils;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.function.Consumer;
import java.util.function.Function;

public class HibernateUtil {

	private static SessionFactory sessionFactory = buildSessionFactory();
    private static Session sharedSession = sessionFactory.openSession();

    private static SessionFactory buildSessionFactory() {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            Configuration cfg = new Configuration().configure();
            if (null != System.getenv("DATABASE_URI")) {
                cfg.setProperty("hibernate.connection.url", System.getenv("DATABASE_URI"));
            }
            return cfg.buildSessionFactory();
        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
    }
    
    public static void shutdown() {
    	// Close caches and connection pools
        sharedSession.close();
    	sessionFactory.close();
    }

    public static <T> T withSession(Function<Session, T> fn) {
        return fn.apply(sharedSession);
    }

    public static void withSession(Consumer<Session> fn) {
        fn.accept(sharedSession);
    }

    public static void restart() {
        shutdown();
        sessionFactory = buildSessionFactory();
        sharedSession = sessionFactory.openSession();
    }
}
