package uk.org.tombolo.core.utils;

import org.hibernate.Session;

import uk.org.tombolo.core.Provider;

public class ProviderUtils {

	public static Provider getTestProvider(){
		Session session = HibernateUtil.getSessionFactory().openSession();
		return (Provider)session.load(Provider.class, "uk.org.tombolo.test");
	}
	
	public static void save(Provider provider){
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		// FIXME: This might be inefficient if we are updating the provider over and over again without actually changing it
		session.saveOrUpdate(provider);
		session.getTransaction().commit();
	}

}
