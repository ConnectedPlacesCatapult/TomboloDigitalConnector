package uk.org.tombolo.core.utils;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.execution.spec.DatasetSpecification;
import uk.org.tombolo.execution.spec.FieldSpecification;
import uk.org.tombolo.execution.spec.SubjectSpecification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static uk.org.tombolo.execution.spec.SubjectSpecification.SubjectMatcher;

public class SubjectUtilsTest extends AbstractTest {

	@Before
	public void addSubjectFixtures() {
		TestFactory.makeNamedSubject("E09000001");
	}

	@Test
	public void testGetSubjectByLabel(){
		Subject subject = SubjectUtils.getSubjectByLabel("E09000001");
		
		assertEquals("E09000001", subject.getLabel());
		assertEquals("City of London", subject.getName());
	}
	
	@Test
	public void testGetSubjectByTypeAndLabelPatternLocalAuthorities(){
		SubjectType localAuthority = SubjectTypeUtils.getSubjectTypeByLabel("localAuthority");
		String labelPattern = null;
		List<Subject> localAuthorities = SubjectUtils.getSubjectByTypeAndLabelPattern(localAuthority, labelPattern);
		
		assertEquals(1, localAuthorities.size());
	}
	
	@Test
	public void testGetSubjectByTypeAndLabelPatternLondonBoroughs(){
		SubjectType localAuthority = SubjectTypeUtils.getSubjectTypeByLabel("localAuthority");
		String labelPattern = "E09%";
		List<Subject> londonBoroughs = SubjectUtils.getSubjectByTypeAndLabelPattern(localAuthority, labelPattern);
		
		assertEquals(1, londonBoroughs.size());
	}

	@Test
	public void testGetSubjectByTypeAndLabelPatternLondonNeighbourhoods(){
		// FIXME: Currently we cannot specify this since we do not have the notion of a parent and the lsoas do not have a label pattern
	}

	@Test
	public void testGetSubjectBySpecificationLabelSearch() throws Exception {
		DatasetSpecification spec = makeDatasetSpecification("label", "E09%", "localAuthority", "uk.gov.london", "populationDensity");
		List<Subject> subjects = SubjectUtils.getSubjectBySpecification(spec);
		assertTrue("Label " + subjects.get(0).getLabel() + " matches searched pattern E09%", subjects.get(0).getLabel().contains("E09"));
	}

	@Test
	public void testGetSubjectBySpecificationNameSearch() throws Exception {
		DatasetSpecification spec = makeDatasetSpecification("name", "%don", "localAuthority", "uk.gov.london", "populationDensity");
		List<Subject> subjects = SubjectUtils.getSubjectBySpecification(spec);
		assertTrue("Name " + subjects.get(0).getName() + " matches searched pattern %don", subjects.get(0).getName().contains("don"));
	}

	@Test
	public void testGetSubjectBySpecificationWithSubject() throws Exception {
		SubjectSpecification spec = makeDatasetSpecification("name", "%don", "localAuthority", "uk.gov.london", "populationDensity").getSubjectSpecification().get(0);
		List<Subject> subjects = SubjectUtils.getSubjectBySpecification(spec);
		assertTrue("Name " + subjects.get(0).getName() + " matches searched pattern %don", subjects.get(0).getName().contains("don"));
	}

	private DatasetSpecification makeDatasetSpecification(String subjectAttribute, String subjectAttributePattern, String subjectType, String attributeProvider, String attributeName) {
		DatasetSpecification spec = new DatasetSpecification();
		List<SubjectSpecification> subjectSpecification = new ArrayList<SubjectSpecification>();
		List<SubjectMatcher> matchers = Arrays.asList(new SubjectMatcher(subjectAttribute, subjectAttributePattern));
		subjectSpecification.add(new SubjectSpecification(matchers, subjectType));
		List<FieldSpecification> fieldSpecification = new ArrayList<FieldSpecification>();
		fieldSpecification.add(new FieldSpecification(attributeProvider, attributeName));
		spec.setSubjectSpecification(subjectSpecification);
		spec.setFieldSpecification(fieldSpecification);
		return spec;
	}
}
