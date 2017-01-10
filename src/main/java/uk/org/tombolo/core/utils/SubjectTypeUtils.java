package uk.org.tombolo.core.utils;

import uk.org.tombolo.core.SubjectType;

import java.util.List;

public class SubjectTypeUtils {
	public static SubjectType getSubjectTypeByLabel(String label){
		return HibernateUtil.withSession(session -> {
			return (SubjectType) session.get(SubjectType.class, label);
		});
	}

	public static void save(List<SubjectType> subjectTypes) {
		for (SubjectType subjectType : subjectTypes) {
			save(subjectType);
		}
	}
	
	public static void save(SubjectType subjectType) {
		HibernateUtil.withSession(session -> {
			session.beginTransaction();
			SubjectType existingSubjectType = (SubjectType) session.get(SubjectType.class, subjectType.getLabel());
			if (existingSubjectType != null) {
				session.update(session.merge(subjectType));
			} else {
				session.save(subjectType);
			}
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
