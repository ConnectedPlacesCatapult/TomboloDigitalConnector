package uk.org.tombolo.core;

import static org.junit.Assert.assertEquals;

import org.hibernate.Session;
import org.junit.Test;

import uk.org.tombolo.core.utils.HibernateUtil;

public class GeographyTypeTest {

	@Test
	public void testFixtures(){
		Session session = HibernateUtil.getSessionFactory().openSession();

		GeographyType unknown = (GeographyType)session.load(GeographyType.class, "unknown");
		assertEquals("unknown", unknown.getLabel());
		assertEquals("Unknown Geography Type", unknown.getName());
		
		GeographyType lsoa = (GeographyType)session.load(GeographyType.class, "lsoa");
		assertEquals("lsoa", lsoa.getLabel());
		assertEquals("Lower Layer Super Output Area", lsoa.getName());

		session.close();

	}
	
}
