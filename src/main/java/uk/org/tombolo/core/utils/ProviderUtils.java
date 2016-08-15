package uk.org.tombolo.core.utils;

import uk.org.tombolo.core.Provider;

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
			return session.createQuery("select p from Provider p where label = :label", Provider.class)
					.setParameter("label", label)
					.uniqueResult();
		});
	}
}
