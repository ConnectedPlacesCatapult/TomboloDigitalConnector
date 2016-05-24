package uk.org.tombolo;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Before;
import uk.org.tombolo.core.utils.HibernateUtil;

public abstract class AbstractTest {
    @Before
    public void clearDatabase() {
        HibernateUtil.restart();
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        try {
            Query truncateTables = session.createSQLQuery("TRUNCATE timed_value, attribute");
            truncateTables.executeUpdate();
            transaction.commit();
        } catch (HibernateException e) {
            transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
}
