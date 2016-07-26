package uk.org.tombolo.core.utils;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import uk.org.tombolo.core.ImportCacheMarker;

import java.util.HashMap;
import java.util.Map;

public class ImportCacheMarkerUtils {
    public static void markCached(String key) {
        HibernateUtil.withSession(session -> {
            session.beginTransaction();
            session.save(new ImportCacheMarker(key));
            session.getTransaction().commit();
        });
    }

    public static boolean isCached(String key) {
        return HibernateUtil.withSession(session -> {
            Criteria criteria = session.createCriteria(ImportCacheMarker.class);
            Map<String, Object> restrictions = new HashMap<String, Object>();
            restrictions.put("key", key);
            return null != criteria.add(Restrictions.allEq(restrictions)).uniqueResult();
        });
    }
}
