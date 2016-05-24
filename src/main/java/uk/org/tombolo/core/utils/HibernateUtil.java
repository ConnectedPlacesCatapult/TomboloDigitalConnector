package uk.org.tombolo.core.utils;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.function.Consumer;
import java.util.function.Function;

public class HibernateUtil {

	private static SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
        	return new Configuration().configure().buildSessionFactory();
        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
    
    public static void shutdown() {
    	// Close caches and connection pools
    	getSessionFactory().close();
    }

    public static <T> T withSession(Function<Session, T> fn) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            return fn.apply(session);
        } finally {
            session.close();
        }
    }

    public static void withSession(Consumer<Session> fn) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            fn.accept(session);
        } finally {
            session.close();
        }
    }

    public static void restart() {
        shutdown();
        sessionFactory = buildSessionFactory();
    }
}
