package uk.org.tombolo;

import org.hibernate.Transaction;
import org.junit.Before;
import uk.org.tombolo.core.utils.HibernateUtil;
import uk.org.tombolo.importer.DownloadUtils;

public abstract class AbstractTest {
    @Before
    public void clearDatabase() {
        // Ensure we aren't clobbering our production DB
        if(!System.getProperty("environment", "").equals("test")) {
            throw new Error("Not running in test mode. You're going to clobber your database!");
        };

        HibernateUtil.restart();
        HibernateUtil.withSession(session -> {
            Transaction transaction = session.beginTransaction();
            session.createSQLQuery("TRUNCATE timed_value, attribute, provider, subject").executeUpdate();
            session.createSQLQuery("DELETE FROM subject_type WHERE label NOT IN ('unknown', 'lsoa', 'msoa', 'localAuthority', 'sensor', 'poi')").executeUpdate();
            transaction.commit();
        });
    }

    protected static DownloadUtils makeTestDownloadUtils() {
        return new DownloadUtils("src/test/resources/datacache");
    }
}
