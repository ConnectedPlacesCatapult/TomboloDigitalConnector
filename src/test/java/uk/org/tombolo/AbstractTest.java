package uk.org.tombolo;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Before;
import uk.org.tombolo.core.utils.HibernateUtil;
import uk.org.tombolo.importer.DownloadUtils;

public abstract class AbstractTest {
    @Before
    public void clearDatabase() {
        HibernateUtil.restart();
        HibernateUtil.withSession(session -> {
            Transaction transaction = session.beginTransaction();
            Query truncateTables = session.createSQLQuery("TRUNCATE timed_value, attribute, provider");
            truncateTables.executeUpdate();
            transaction.commit();
        });
    }

    protected static DownloadUtils makeTestDownloadUtils() {
        return new DownloadUtils("src/test/resources/datacache");
    }
}
