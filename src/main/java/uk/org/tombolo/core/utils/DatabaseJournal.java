package uk.org.tombolo.core.utils;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import uk.org.tombolo.core.DatabaseJournalEntry;

import java.util.HashMap;
import java.util.Map;

/**
 * DatabaseJournal.java
 * The DatabaseJournal is a list of transformations that the database has undergone.
 * For example imports. This is used so we can avoid repeating the job later on,
 * as well as a description of the transforms that this database has undergone.
 */
public class DatabaseJournal {
    public static void addJournalEntry(DatabaseJournalEntry entry) {
        HibernateUtil.withSession(session -> {
            session.beginTransaction();
            session.save(entry);
            session.getTransaction().commit();
        });
    }

    public static boolean journalHasEntry(DatabaseJournalEntry entry) {
        return HibernateUtil.withSession(session -> {
            Criteria criteria = session.createCriteria(DatabaseJournalEntry.class);
            Map<String, Object> restrictions = new HashMap<String, Object>();
            restrictions.put("className", entry.getClassName());
            restrictions.put("key", entry.getKey());
            return null != criteria.add(Restrictions.allEq(restrictions)).uniqueResult();
        });
    }
}
