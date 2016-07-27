package uk.org.tombolo.core.utils;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import uk.org.tombolo.core.DatabaseJournalEntry;

import java.util.HashMap;
import java.util.Map;

public class DatabaseJournal {
    public static void logJobComplete(DatabaseJournalEntry entry) {
        HibernateUtil.withSession(session -> {
            session.beginTransaction();
            session.save(entry);
            session.getTransaction().commit();
        });
    }

    public static boolean jobHasBeenDone(DatabaseJournalEntry entry) {
        return HibernateUtil.withSession(session -> {
            Criteria criteria = session.createCriteria(DatabaseJournalEntry.class);
            Map<String, Object> restrictions = new HashMap<String, Object>();
            restrictions.put("id", entry.getId());
            return null != criteria.add(Restrictions.allEq(restrictions)).uniqueResult();
        });
    }
}
