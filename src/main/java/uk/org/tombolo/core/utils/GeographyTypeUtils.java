package uk.org.tombolo.core.utils;

import org.hibernate.Session;

import uk.org.tombolo.core.GeographyType;

public class GeographyTypeUtils {

	public static GeographyType getUnknowhGeographyType(){
		Session session = HibernateUtil.getSessionFactory().openSession();
		GeographyType unknown = (GeographyType)session.load(GeographyType.class, "unknown");
		return unknown;
	}
}
