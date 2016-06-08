package uk.org.tombolo.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.utils.HibernateUtil;

public class SubjectTypeTest extends AbstractTest {

	@Test
	public void testFixtures(){
		HibernateUtil.withSession(session -> {
			SubjectType unknown = (SubjectType)session.load(SubjectType.class, "unknown");
			assertEquals("unknown", unknown.getLabel());
			assertEquals("Unknown Subject Type", unknown.getName());

			SubjectType lsoa = (SubjectType)session.load(SubjectType.class, "lsoa");
			assertEquals("lsoa", lsoa.getLabel());
			assertEquals("Lower Layer Super Output Area", lsoa.getName());
		});
	}
	
}
