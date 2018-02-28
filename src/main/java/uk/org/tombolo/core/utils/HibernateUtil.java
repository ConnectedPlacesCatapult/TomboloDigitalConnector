package uk.org.tombolo.core.utils;

import org.hibernate.CacheMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
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

    public static void startUpForPython() {
        sessionFactory = buildSessionFactoryForPython();
        sharedSession = sessionFactory.openSession();
        sharedSession.setCacheMode(CacheMode.NORMAL);
    }

    private static SessionFactory buildSessionFactoryForPython() {
        SessionFactory factory;
        StandardServiceRegistry registry;
        try {
            StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();
            Map<String, String> settings = new HashMap<>();
            settings.put(Environment.DRIVER, "org.postgresql.Driver");
            settings.put(Environment.URL, "jdbc:postgresql://localhost:5432/tombolo");
            settings.put(Environment.USER, "tombolo");
            settings.put(Environment.PASS, "tombolo");
            settings.put(Environment.DIALECT, "org.hibernate.spatial.dialect.postgis.PostgisDialect");
            
            registryBuilder.applySettings(settings);
            registry = registryBuilder.build();
            MetadataSources sources = new MetadataSources(registry)
            .addAnnotatedClass(uk.org.tombolo.core.Provider.class)
            .addAnnotatedClass(uk.org.tombolo.core.Attribute.class)
            .addAnnotatedClass(uk.org.tombolo.core.Subject.class)
            .addAnnotatedClass(uk.org.tombolo.core.SubjectType.class)
            .addAnnotatedClass(uk.org.tombolo.core.FixedValue.class)
            .addAnnotatedClass(uk.org.tombolo.core.TimedValue.class);

            Metadata metadata = sources.getMetadataBuilder().build();
            factory = metadata.getSessionFactoryBuilder().build();
            
        } catch (Exception ex) {
            throw new ServiceConfigurationError("Failed to create SessionFactory", ex);
        }

        return factory;
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
