package uk.org.tombolo.core.utils;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import uk.org.tombolo.core.Geography;

public class GeographyUtils {

	public static Geography getTestGeography(){
		Session session = HibernateUtil.getSessionFactory().openSession();

		Criteria criteria = session.createCriteria(Geography.class);
		return (Geography)criteria.add(Restrictions.eq("label", "E01000001")).uniqueResult();
	}
}
