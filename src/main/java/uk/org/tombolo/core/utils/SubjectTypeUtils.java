package uk.org.tombolo.core.utils;

import uk.org.tombolo.core.SubjectType;

public class SubjectTypeUtils {
	public static SubjectType getGeographyTypeByLabel(String label){
		return HibernateUtil.withSession(session -> {
			return (SubjectType) session.get(SubjectType.class, label);
		});
	}
	
	public static void save(SubjectType subjectType){
		HibernateUtil.withSession(session -> {
			session.beginTransaction();
			session.saveOrUpdate(subjectType);
			session.getTransaction().commit();
		});
	}
}
