package uk.org.tombolo.core.utils;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import uk.org.tombolo.core.Provider;

import java.util.HashMap;
import java.util.Map;

public class ProviderUtils {
	public static Provider getTestProvider(){
		return HibernateUtil.withSession(session -> {
			return (Provider)session.get(Provider.class, "uk.org.tombolo.test");
		});
	}
	
	public static void save(Provider provider){
		HibernateUtil.withSession(session -> {
			session.beginTransaction();
			// FIXME: This might be inefficient if we are updating the provider over and over again without actually changing it
			Provider savedProvider = (Provider) session.get(Provider.class, provider.getLabel());
			if (savedProvider != null) {
				session.update(session.merge(provider));
			} else {
				session.save(provider);
			}
			session.getTransaction().commit();
		});
	}

	public static Provider getByLabel(String label){
		return HibernateUtil.withSession(session -> {
			Criteria criteria = session.createCriteria(Provider.class);
			Map<String, Object> restrictions = new HashMap<String, Object>();
			restrictions.put("label", label);
			Provider provider = (Provider) criteria.add(Restrictions.allEq(restrictions)).uniqueResult();
			return provider;
		});
	}
}
