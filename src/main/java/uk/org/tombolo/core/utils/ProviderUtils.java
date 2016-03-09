package uk.org.tombolo.core.utils;

import org.hibernate.Session;

import uk.org.tombolo.core.Provider;

public class ProviderUtils {

	public static Provider getTestProvider(){
		Session session = HibernateUtil.getSessionFactory().openSession();
		return (Provider)session.load(Provider.class, "uk.org.tombolo.test");
	}
}
