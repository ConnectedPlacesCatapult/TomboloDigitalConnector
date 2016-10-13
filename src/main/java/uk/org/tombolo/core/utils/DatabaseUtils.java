package uk.org.tombolo.core.utils;

import org.hibernate.Transaction;

public class DatabaseUtils {
    public static void clearAllData() {
        HibernateUtil.restart(); // We need to do this to clear the data from the session
        HibernateUtil.withSession(session -> {
            Transaction transaction = session.beginTransaction();
            session.createSQLQuery("TRUNCATE timed_value, fixed_value, attribute, provider, subject, database_journal").executeUpdate();
            session.createSQLQuery("DELETE FROM subject_type WHERE label NOT IN ('unknown', 'sensor', 'poi')").executeUpdate();
            transaction.commit();
        });
    }
}
