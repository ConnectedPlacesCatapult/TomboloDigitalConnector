package uk.org.tombolo.core.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import uk.org.tombolo.core.GeographyType;

public class GeographyTypeUtilsTest {

	@Test
	public void testGetUnknownGeographyType(){
		GeographyType unknown = GeographyTypeUtils.getUnknowhGeographyType();
		assertEquals("unknown", unknown.getLabel());
		assertEquals("Unknown Geography Type", unknown.getName());
	}
}
