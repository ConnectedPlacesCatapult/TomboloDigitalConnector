package uk.org.tombolo.core.utils;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import uk.org.tombolo.core.Geography;
import uk.org.tombolo.core.GeographyType;

public class GeographyUtilsTest {

	@Test
	public void testGetGeographyByLabel(){
		Geography geography = GeographyUtils.getGeographyByLabel("E09000026");
		
		assertEquals("E09000026", geography.getLabel());
		assertEquals("Redbridge", geography.getName());
	}
	
	@Test
	public void testGetGeographyByTypeAndLabelPatternLocalAuthorities(){
		GeographyType localAuthority = GeographyTypeUtils.getGeographyTypeByLabel("localAuthority");
		String labelPattern = null;
		List<Geography> localAuthorities = GeographyUtils.getGeographyByTypeAndLabelPattern(localAuthority, labelPattern);
		
		assertEquals(380, localAuthorities.size());		
	}
	
	@Test
	public void testGetGeographyByTypeAndLabelPatternLondonBoroughs(){
		GeographyType localAuthority = GeographyTypeUtils.getGeographyTypeByLabel("localAuthority");
		String labelPattern = "E09%";
		List<Geography> londonBoroughs = GeographyUtils.getGeographyByTypeAndLabelPattern(localAuthority, labelPattern);
		
		assertEquals(33, londonBoroughs.size());
	}

	@Test
	public void testGetGeographyByTypeAndLabelPatternLondonNeighbourhoods(){
		// FIXME: Currently we cannot specify this since we do not have the notion of a parent and the lsoas do not have a label pattern
	}
}
