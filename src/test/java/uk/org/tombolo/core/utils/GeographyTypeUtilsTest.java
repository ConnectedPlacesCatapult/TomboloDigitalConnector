package uk.org.tombolo.core.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.GeographyType;

public class GeographyTypeUtilsTest extends AbstractTest {

	@Test
	public void testGetUnknownGeographyType(){
		GeographyType unknown = GeographyTypeUtils.getUnknowhGeographyType();
		assertEquals("unknown", unknown.getLabel());
		assertEquals("Unknown Geography Type", unknown.getName());
	}
	
	@Test
	public void testGetGeographyTypeByLabel(){
		GeographyType lsoa = GeographyTypeUtils.getGeographyTypeByLabel("lsoa");
		assertEquals("lsoa", lsoa.getLabel());
		assertEquals("Lower Layer Super Output Area", lsoa.getName());
	}
}
