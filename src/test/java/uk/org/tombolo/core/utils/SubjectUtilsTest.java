package uk.org.tombolo.core.utils;

import static org.junit.Assert.*;
import static uk.org.tombolo.execution.spec.SubjectSpecification.*;

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
import uk.org.tombolo.execution.spec.SubjectSpecification;

public class SubjectUtilsTest extends AbstractTest {

	@Before
	public void addGeography() {
		TestFactory.makeNamedGeography("E09000001");
	}

	@Test
	public void testGetGeographyByLabel(){
		Subject geography = SubjectUtils.getSubjectByLabel("E09000001");
		
		assertEquals("E09000001", geography.getLabel());
		assertEquals("City of London", geography.getName());
	}
	
	@Test
	public void testGetGeographyByTypeAndLabelPatternLocalAuthorities(){
		SubjectType localAuthority = SubjectTypeUtils.getSubjectTypeByLabel("localAuthority");
		String labelPattern = null;
		List<Subject> localAuthorities = SubjectUtils.getSubjectByTypeAndLabelPattern(localAuthority, labelPattern);
		
		assertEquals(1, localAuthorities.size());
	}
	
	@Test
	public void testGetGeographyByTypeAndLabelPatternLondonBoroughs(){
		SubjectType localAuthority = SubjectTypeUtils.getSubjectTypeByLabel("localAuthority");
		String labelPattern = "E09%";
		List<Subject> londonBoroughs = SubjectUtils.getSubjectByTypeAndLabelPattern(localAuthority, labelPattern);
		
		assertEquals(1, londonBoroughs.size());
	}

	@Test
	public void testGetGeographyByTypeAndLabelPatternLondonNeighbourhoods(){
		// FIXME: Currently we cannot specify this since we do not have the notion of a parent and the lsoas do not have a label pattern
	}

	@Test
	public void testGetGeographyBySpecificationLabelSearch() throws Exception {
		DatasetSpecification spec = makeDatasetSpecification("label", "E09%", "localAuthority", "uk.gov.london", "populationDensity");
		List<Subject> geographies = SubjectUtils.getSubjectBySpecification(spec);
		assertTrue("Label " + geographies.get(0).getLabel() + " matches searched pattern E09%", geographies.get(0).getLabel().contains("E09"));
	}

	@Test
	public void testGetGeographyBySpecificationNameSearch() throws Exception {
		DatasetSpecification spec = makeDatasetSpecification("name", "%don", "localAuthority", "uk.gov.london", "populationDensity");
		List<Subject> geographies = SubjectUtils.getSubjectBySpecification(spec);
		assertTrue("Name " + geographies.get(0).getName() + " matches searched pattern %don", geographies.get(0).getName().contains("don"));
	}

	@Test
	public void testGetGeographyBySpecificationWithGeography() throws Exception {
		SubjectSpecification spec = makeDatasetSpecification("name", "%don", "localAuthority", "uk.gov.london", "populationDensity").getSubjectSpecification().get(0);
		List<Subject> geographies = SubjectUtils.getSubjectBySpecification(spec);
		assertTrue("Name " + geographies.get(0).getName() + " matches searched pattern %don", geographies.get(0).getName().contains("don"));
	}

	private DatasetSpecification makeDatasetSpecification(String geographyAttribute, String geographyAttributePattern, String geographyType, String attributeProvider, String attributeName) {
		DatasetSpecification spec = new DatasetSpecification();
		List<SubjectSpecification> subjectSpecification = new ArrayList<SubjectSpecification>();
		List<SubjectMatcher> matchers = Arrays.asList(new SubjectMatcher(geographyAttribute, geographyAttributePattern));
		subjectSpecification.add(new SubjectSpecification(matchers, geographyType));
		List<AttributeSpecification> attributeSpecification = new ArrayList<AttributeSpecification>();
		attributeSpecification.add(new AttributeSpecification(attributeProvider, attributeName));
		spec.setSubjectSpecification(subjectSpecification);
		spec.setAttributeSpecification(attributeSpecification);
		return spec;
	}
}
