package uk.org.tombolo.core.utils;

import org.hibernate.Session;
import org.junit.Test;

import static org.junit.Assert.*;

public class HibernateUtilTest {

    @Test
    public void testWithSessionFunctionProvidesSession() throws Exception {
        String retVal = HibernateUtil.withSession((Session session) -> {
            // Type system guarantees it's a session, we check for null though
            assertNotNull(session);
            return ""; // We need this, otherwise this lambda isn't a function & we're testing the consumer version
        });
    }

    @Test
    public void testWithSessionFunctionReturns() throws Exception {
        String retVal = HibernateUtil.withSession(session -> {
            return "good_string";
        });

        assertEquals("good_string", retVal);
    }

    @Test
    public void testWithSessionConsumerProvidesSession() throws Exception {
        HibernateUtil.withSession((Session session) -> {
            assertNotNull(session);
        });
    }

    @Test
    public void testWithSessionConsumerDoesCall() throws Exception {
        final Boolean[] flag = {false};
        HibernateUtil.withSession(session -> {
            flag[0] = true;
        });

        assertTrue(flag[0]);
    }
}