package uk.org.tombolo.core.utils;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import uk.org.tombolo.core.ImportCacheMarker;

import java.util.HashMap;
import java.util.Map;

public class ImportCacheMarkerUtils {
    public static void markCached(ImportCacheMarker.ImportCacheMarkerId id) {
        HibernateUtil.withSession(session -> {
            session.beginTransaction();
            session.save(new ImportCacheMarker(id));
            session.getTransaction().commit();
        });
    }

    public static boolean isCached(ImportCacheMarker.ImportCacheMarkerId id) {
        return HibernateUtil.withSession(session -> {
            Criteria criteria = session.createCriteria(ImportCacheMarker.class);
            Map<String, Object> restrictions = new HashMap<String, Object>();
            restrictions.put("id", id);
            return null != criteria.add(Restrictions.allEq(restrictions)).uniqueResult();
        });
    }
}
