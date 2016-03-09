package uk.org.tombolo.core.utils;

import org.hibernate.Session;

import uk.org.tombolo.core.TimedValue;

public class TimedValueUtils {

	static Session session = HibernateUtil.getSessionFactory().openSession();

	public static void save(TimedValue timedValue){
		session.beginTransaction();
		session.saveOrUpdate(timedValue);
		session.getTransaction().commit();
	}
	
}
