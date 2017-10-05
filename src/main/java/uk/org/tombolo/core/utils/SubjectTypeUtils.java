package uk.org.tombolo.core.utils;

import org.hibernate.query.Query;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.SubjectType;

import java.util.List;

public class SubjectTypeUtils {

	public static void save(List<SubjectType> subjectTypes) {
		for (SubjectType subjectType : subjectTypes) {
			save(subjectType);
		}
	}
	
	public static void save(SubjectType subjectType) {
		ProviderUtils.save(subjectType.getProvider());
		HibernateUtil.withSession(session -> {
			session.beginTransaction();
			SubjectType existingSubjectType = getSubjectTypeByProviderAndLabel(subjectType.getProvider().getLabel(), subjectType.getLabel());
			if (existingSubjectType != null) {
				subjectType.setId(existingSubjectType.getId());
				session.update(session.merge(subjectType));
			} else {
				session.save(subjectType);
			}
			session.getTransaction().commit();
		});
	}

	public static SubjectType getOrCreate(Provider provider, String label, String name) {
		SubjectType subjectType = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(provider.getLabel(), label);
		if (null == subjectType) {
			subjectType = new SubjectType(provider, label, name);
			SubjectTypeUtils.save(subjectType);
		}

		return subjectType;
	}

	public static SubjectType getSubjectTypeByProviderAndLabel(String providerLabel, String subjectTypeLabel) {
		return HibernateUtil.withSession(session -> {
			Query query =  session.createQuery("from SubjectType where label = :subjectTypeLabel and provider.label = :providerLabel", SubjectType.class)
					.setParameter("providerLabel", providerLabel)
					.setParameter("subjectTypeLabel", subjectTypeLabel);
			query.setCacheable(true);
			return (SubjectType) query.uniqueResult();
		});
	}
}
