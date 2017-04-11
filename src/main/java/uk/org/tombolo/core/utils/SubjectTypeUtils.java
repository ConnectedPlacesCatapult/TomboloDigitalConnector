package uk.org.tombolo.core.utils;

import org.hibernate.query.Query;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.SubjectType;

import java.util.List;

public class SubjectTypeUtils {
	public static SubjectType getSubjectTypeByLabel(String label){
		return getSubjectTypeByProviderAndLabel("default_provider_label", label);
	}

	public static void save(List<SubjectType> subjectTypes) {
		for (SubjectType subjectType : subjectTypes) {
			save(subjectType);
		}
	}
	
	public static void save(SubjectType subjectType) {
		HibernateUtil.withSession(session -> {
			session.beginTransaction();
			SubjectType existingSubjectType = getSubjectTypeByProviderAndLabel(subjectType.getProvider().getLabel(), subjectType.getLabel());

			if (existingSubjectType != null && !existingSubjectType.equals(subjectType)) {
				session.update(session.merge(subjectType));
			} else {
				session.save(subjectType);
			}
			session.getTransaction().commit();
		});
	}

	public static SubjectType getOrCreate(Provider provider, String label, String description) {
		SubjectType subjectType = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(provider.getLabel(), label);
		if (null == subjectType) {
			subjectType = new SubjectType(provider, label, description);
			SubjectTypeUtils.save(subjectType);
		}

		return subjectType;
	}

	public static SubjectType getSubjectTypeByProviderAndLabel(String providerLabel, String subjectTypeLabel) {
		return HibernateUtil.withSession(session -> {
			return session.createQuery("from SubjectType where label = :subjectTypeLabel and provider.label = :providerLabel", SubjectType.class)
				.setParameter("providerLabel", providerLabel)
				.setParameter("subjectTypeLabel", subjectTypeLabel)
				.uniqueResult();
		});
	}
}
