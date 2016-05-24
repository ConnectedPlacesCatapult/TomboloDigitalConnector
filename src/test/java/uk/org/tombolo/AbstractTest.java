package uk.org.tombolo;

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
        try {
            Transaction transaction = session.beginTransaction();
            Query truncateTimedValue = session.createSQLQuery("TRUNCATE timed_value");
            truncateTimedValue.executeUpdate();
            transaction.commit();
        } finally {
            session.close();
        }
    }
}
