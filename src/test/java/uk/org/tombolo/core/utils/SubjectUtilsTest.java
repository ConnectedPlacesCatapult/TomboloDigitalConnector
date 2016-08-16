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
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static uk.org.tombolo.execution.spec.SubjectSpecification.SubjectMatchRule;

public class SubjectUtilsTest extends AbstractTest {

	@Before
	public void addSubjectFixtures() {
		TestFactory.makeNamedSubject("E09000001");
		TestFactory.makeNamedSubject("E08000035"); // Need this to avoid false-positives on pattern matching
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
		List<Subject> localAuthorities = SubjectUtils.getSubjectByTypeAndLabelPattern(localAuthority, "%");
		
		assertEquals(2, localAuthorities.size());
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
		SubjectMatchRule matchRule = new SubjectSpecification.SubjectMatchRule(SubjectMatchRule.MatchableAttribute.valueOf(subjectAttribute), subjectAttributePattern);
		subjectSpecification.add(new SubjectSpecification(matchRule, subjectType));
		List<FieldSpecification> fieldSpecification = new ArrayList<FieldSpecification>();
		fieldSpecification.add(new FieldSpecification(attributeProvider, attributeName));
		spec.setSubjectSpecification(subjectSpecification);
		spec.setFieldSpecification(fieldSpecification);
		return spec;
	}

	@Test
	public void testSubjectsContainingSubjectReturnsContainingSubject() throws Exception {
		Subject cityOfLondon = SubjectUtils.getSubjectByLabel("E09000001");
		Subject cityOfLondonLsoa = TestFactory.makeNamedSubject("E01000001");
		cityOfLondon.setShape(TestFactory.makePointGeometry(100d, 100d));
		cityOfLondonLsoa.setShape(TestFactory.makePointGeometry(100d, 100d)); // make them overlap
		SubjectUtils.save(Arrays.asList(cityOfLondon, cityOfLondonLsoa));

		List<Subject> returnedSubjects = SubjectUtils.subjectsContainingSubject("localAuthority", cityOfLondonLsoa);
		assertEquals(cityOfLondon, returnedSubjects.get(0));
		assertEquals(1, returnedSubjects.size());
	}


	@Test
	public void testSubjectsContainingSubjectReturnsNullOnWrongSubjectType() throws Exception {
		Subject cityOfLondonLsoa = TestFactory.makeNamedSubject("E01000001"); // Subject contained by 'City of London'
		List<Subject> returnedSubjects = SubjectUtils.subjectsContainingSubject("msoa", cityOfLondonLsoa);
		assertEquals(0, returnedSubjects.size());
	}

	@Test
	public void testSubjectsContainingSubjectReturnsNullOnNoContainingSubject() throws Exception {
		Subject cityOfLondon = SubjectUtils.getSubjectByLabel("E09000001");
		// We make Islington, but our fake geoms are all 0, 0 - so we move it a unit away
		Subject islingtonLsoa = TestFactory.makeNamedSubject("E01002766");
		islingtonLsoa.setShape(TestFactory.makePointGeometry(1d, 1d));
		SubjectUtils.save(Collections.singletonList(islingtonLsoa));

		List<Subject> returnedSubjects = SubjectUtils.subjectsContainingSubject("localAuthority", islingtonLsoa);
		assertEquals(0, returnedSubjects.size());
	}
}
