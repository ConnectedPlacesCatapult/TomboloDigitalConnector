package uk.org.tombolo.core.utils;

import org.hibernate.query.Query;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Provider;

import java.util.Arrays;
import java.util.List;

public class AttributeUtils {
	public static void save(List<Attribute> attributes){
		HibernateUtil.withSession(session -> {
			session.beginTransaction();
			for (Attribute attribute : attributes) {
				// FIXME: This might be inefficient if we are updating the attribute over and over again without actually changing it
				Attribute savedAttribute = getByProviderAndLabel(attribute.getProvider(), attribute.getLabel());
				if (savedAttribute == null) {
					Integer id = (Integer) session.save(attribute);
					attribute.setId(id);
				} else {
					attribute.setId(savedAttribute.getId());
					savedAttribute.setProvider(attribute.getProvider());
					savedAttribute.setLabel(attribute.getLabel());
					savedAttribute.setDescription(attribute.getDescription());
					session.save(savedAttribute);
				}
			}
			session.getTransaction().commit();
		});
	}

	public static void save(Attribute attribute) {
		save(Arrays.asList(attribute));
	}

	public static Attribute getByProviderAndLabel(Provider provider, String label){
		return getByProviderAndLabel(provider.getLabel(), label);
	}

	public static Attribute getByProviderAndLabel(String providerLabel, String attributeLabel) {
		return HibernateUtil.withSession(session -> {
			Query query = session.createQuery("from Attribute where provider.label = :providerLabel and label = :attributeLabel", Attribute.class);
			query.setCacheable(true);
			query.setParameter("providerLabel", providerLabel);
			query.setParameter("attributeLabel", attributeLabel);
			return (Attribute) query.uniqueResult();
		});
	}
}
