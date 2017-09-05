package uk.org.tombolo.core.utils;

import org.hibernate.Transaction;

public class DatabaseUtils {
    public static void clearAllData() {
        HibernateUtil.restart(); // We need to do this to clear the data from the session
        HibernateUtil.withSession(session -> {
            Transaction transaction = session.beginTransaction();
            session.createNativeQuery("TRUNCATE timed_value, fixed_value, attribute, subject, database_journal").executeUpdate();
            session.createNativeQuery("DELETE FROM subject_type WHERE label NOT IN ('unknown', 'poi')").executeUpdate();
            session.createNativeQuery("DELETE FROM provider WHERE label NOT IN ('default_provider_label')").executeUpdate();
            transaction.commit();
        });
    }
}
