package uk.org.tombolo.core.utils;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import uk.org.tombolo.core.Geography;
import uk.org.tombolo.core.GeographyType;
import uk.org.tombolo.execution.spec.AttributeSpecification;
import uk.org.tombolo.execution.spec.DatasetSpecification;
import uk.org.tombolo.execution.spec.GeographySpecification;

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
		
		// FIXME: This was based on the local authorities shapefile that magically disappeared from the web 
		//assertEquals(380, localAuthorities.size());
		
		assertEquals(174, localAuthorities.size());
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

	@Test
	public void testGetGeographyBySpecificationLabelSearch() throws Exception {
		List<Geography> geographies = GeographyUtils.getGeographyBySpecification(makeDatasetSpecification("E09%", "localAuthority", "uk.gov.london", "populationDensity"));
		assertTrue("Label " + geographies.get(0).getLabel() + " matches searched pattern E09%", geographies.get(0).getLabel().contains("E09"));
	}

	private DatasetSpecification makeDatasetSpecification(String geographyLabelPattern, String geographyType, String attributeProvider, String attributeName) {
		DatasetSpecification spec = new DatasetSpecification();
		List<GeographySpecification> geographySpecification = new ArrayList<GeographySpecification>();
		geographySpecification.add(new GeographySpecification(geographyLabelPattern, geographyType));
		List<AttributeSpecification> attributeSpecification = new ArrayList<AttributeSpecification>();
		attributeSpecification.add(new AttributeSpecification(attributeProvider, attributeName));
		spec.setGeographySpecification(geographySpecification);
		spec.setAttributeSpecification(attributeSpecification);
		return spec;
	}
}
