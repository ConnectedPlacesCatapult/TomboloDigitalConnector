package uk.org.tombolo.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.junit.AfterClass;
import org.junit.Test;

import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.utils.GeographyUtils;
import uk.org.tombolo.core.utils.HibernateUtil;

public class GeographyTest extends AbstractTest {

	@Test
	public void testLsoaLoad(){
		HibernateUtil.withSession(session -> {
			Criteria criteria = session.createCriteria(Geography.class);

			Geography cityOfLondon1A = (Geography)criteria.add(Restrictions.eq("label", "E01000001")).uniqueResult();
			assertEquals("lsoa", cityOfLondon1A.getGeographyType().getLabel());
			assertEquals("Lower Layer Super Output Area", cityOfLondon1A.getGeographyType().getName());
			assertEquals("City of London 001A", cityOfLondon1A.getName());
		});
	}

	@Test
	public void testGeometryOverlap(){
		HibernateUtil.withSession(session -> {
			Criteria criteria = session.createCriteria(Geography.class);

			Geography cityOfLondon = GeographyUtils.getGeographyByLabel("E09000001");
			Geography cityOfLondon1A = (Geography) criteria.add(Restrictions.eq("label", "E01000001")).uniqueResult();

			assertTrue(cityOfLondon.getShape().contains(cityOfLondon1A.getShape()));
			assertFalse(cityOfLondon1A.getShape().contains(cityOfLondon.getShape()));
		});
	}
	
	
}
