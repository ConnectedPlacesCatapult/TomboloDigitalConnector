package uk.org.tombolo.core.utils;

import static org.junit.Assert.*;
import static uk.org.tombolo.execution.spec.GeographySpecification.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.execution.spec.AttributeSpecification;
import uk.org.tombolo.execution.spec.DatasetSpecification;
import uk.org.tombolo.execution.spec.GeographySpecification;

public class SubjectUtilsTest extends AbstractTest {

	@Before
	public void addGeography() {
		TestFactory.makeNamedGeography("E09000001");
	}

	@Test
	public void testGetGeographyByLabel(){
		Subject geography = SubjectUtils.getGeographyByLabel("E09000001");
		
		assertEquals("E09000001", geography.getLabel());
		assertEquals("City of London", geography.getName());
	}
	
	@Test
	public void testGetGeographyByTypeAndLabelPatternLocalAuthorities(){
		SubjectType localAuthority = SubjectTypeUtils.getGeographyTypeByLabel("localAuthority");
		String labelPattern = null;
		List<Subject> localAuthorities = SubjectUtils.getGeographyByTypeAndLabelPattern(localAuthority, labelPattern);
		
		assertEquals(1, localAuthorities.size());
	}
	
	@Test
	public void testGetGeographyByTypeAndLabelPatternLondonBoroughs(){
		SubjectType localAuthority = SubjectTypeUtils.getGeographyTypeByLabel("localAuthority");
		String labelPattern = "E09%";
		List<Subject> londonBoroughs = SubjectUtils.getGeographyByTypeAndLabelPattern(localAuthority, labelPattern);
		
		assertEquals(1, londonBoroughs.size());
	}

	@Test
	public void testGetGeographyByTypeAndLabelPatternLondonNeighbourhoods(){
		// FIXME: Currently we cannot specify this since we do not have the notion of a parent and the lsoas do not have a label pattern
	}

	@Test
	public void testGetGeographyBySpecificationLabelSearch() throws Exception {
		DatasetSpecification spec = makeDatasetSpecification("label", "E09%", "localAuthority", "uk.gov.london", "populationDensity");
		List<Subject> geographies = SubjectUtils.getGeographyBySpecification(spec);
		assertTrue("Label " + geographies.get(0).getLabel() + " matches searched pattern E09%", geographies.get(0).getLabel().contains("E09"));
	}

	@Test
	public void testGetGeographyBySpecificationNameSearch() throws Exception {
		DatasetSpecification spec = makeDatasetSpecification("name", "%don", "localAuthority", "uk.gov.london", "populationDensity");
		List<Subject> geographies = SubjectUtils.getGeographyBySpecification(spec);
		assertTrue("Name " + geographies.get(0).getName() + " matches searched pattern %don", geographies.get(0).getName().contains("don"));
	}

	@Test
	public void testGetGeographyBySpecificationWithGeography() throws Exception {
		GeographySpecification spec = makeDatasetSpecification("name", "%don", "localAuthority", "uk.gov.london", "populationDensity").getGeographySpecification().get(0);
		List<Subject> geographies = SubjectUtils.getGeographyBySpecification(spec);
		assertTrue("Name " + geographies.get(0).getName() + " matches searched pattern %don", geographies.get(0).getName().contains("don"));
	}

	private DatasetSpecification makeDatasetSpecification(String geographyAttribute, String geographyAttributePattern, String geographyType, String attributeProvider, String attributeName) {
		DatasetSpecification spec = new DatasetSpecification();
		List<GeographySpecification> geographySpecification = new ArrayList<GeographySpecification>();
		List<GeographyMatcher> matchers = Arrays.asList(new GeographyMatcher(geographyAttribute, geographyAttributePattern));
		geographySpecification.add(new GeographySpecification(matchers, geographyType));
		List<AttributeSpecification> attributeSpecification = new ArrayList<AttributeSpecification>();
		attributeSpecification.add(new AttributeSpecification(attributeProvider, attributeName));
		spec.setGeographySpecification(geographySpecification);
		spec.setAttributeSpecification(attributeSpecification);
		return spec;
	}
}
