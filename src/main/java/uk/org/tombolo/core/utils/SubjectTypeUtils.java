package uk.org.tombolo.core.utils;

import uk.org.tombolo.core.SubjectType;

public class SubjectTypeUtils {
	public static SubjectType getSubjectTypeByLabel(String label){
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

	public static SubjectType getOrCreate(String label, String description) {
		SubjectType subjectType = SubjectTypeUtils.getSubjectTypeByLabel(label);
		if (null == subjectType) {
			subjectType = new SubjectType(label, description);
			SubjectTypeUtils.save(subjectType);
		}

		return subjectType;
	}
}
