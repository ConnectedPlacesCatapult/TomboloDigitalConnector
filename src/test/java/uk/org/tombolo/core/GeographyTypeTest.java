package uk.org.tombolo.core;

import static org.junit.Assert.assertEquals;

import org.hibernate.Session;
import org.junit.Test;

import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.utils.HibernateUtil;

public class GeographyTypeTest extends AbstractTest {

	@Test
	public void testFixtures(){
		HibernateUtil.withSession(session -> {
			GeographyType unknown = (GeographyType)session.load(GeographyType.class, "unknown");
			assertEquals("unknown", unknown.getLabel());
			assertEquals("Unknown Geography Type", unknown.getName());

			GeographyType lsoa = (GeographyType)session.load(GeographyType.class, "lsoa");
			assertEquals("lsoa", lsoa.getLabel());
			assertEquals("Lower Layer Super Output Area", lsoa.getName());
		});
	}
	
}
