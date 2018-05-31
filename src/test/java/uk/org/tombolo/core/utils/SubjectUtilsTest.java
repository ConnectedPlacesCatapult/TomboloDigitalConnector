package uk.org.tombolo.core.utils;

import com.vividsolutions.jts.geom.Geometry;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.DataExportSpecificationBuilder;
import uk.org.tombolo.SubjectSpecificationBuilder;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.recipe.DatasetRecipe;
import uk.org.tombolo.recipe.SubjectRecipe;
import uk.org.tombolo.importer.ons.AbstractONSImporter;
import uk.org.tombolo.importer.ons.OaImporter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class SubjectUtilsTest extends AbstractTest {
	private SubjectType localAuthority;
	private SubjectType msoa;
	private SubjectType lsoa;
	private Subject cityOfLondon;

	@Before
	public void addSubjectFixtures() {
		cityOfLondon = TestFactory.makeNamedSubject( "E09000001");
		TestFactory.makeNamedSubject("E08000035"); // Need this to avoid false-positives on pattern matching
		localAuthority = SubjectTypeUtils.getOrCreate(AbstractONSImporter.PROVIDER,
				OaImporter.OaType.localAuthority.name(), OaImporter.OaType.localAuthority.datasourceSpec.getDescription());
		msoa = SubjectTypeUtils.getOrCreate(AbstractONSImporter.PROVIDER,
				OaImporter.OaType.msoa.name(), OaImporter.OaType.msoa.datasourceSpec.getDescription());
		lsoa = SubjectTypeUtils.getOrCreate(AbstractONSImporter.PROVIDER,
				OaImporter.OaType.lsoa.name(), OaImporter.OaType.lsoa.datasourceSpec.getDescription());
	}

	@Test
	public void testSave(){
		SubjectType testSubjectType = TestFactory.makeSubjectType(TestFactory.DEFAULT_PROVIDER, "awsomeSubjectType1", "Awasome Subject Type");
		Geometry geometry = TestFactory.makePointGeometry(1d,1d);

		Subject subject1 = new Subject(testSubjectType, "subject1", "Subject 1a", TestFactory.FAKE_POINT_GEOMETRY);
		Subject subject2 = new Subject(testSubjectType, "subject1", "Subject 1b", TestFactory.FAKE_POINT_GEOMETRY);
		Subject subject3 = new Subject(testSubjectType, "subject1", "Subject 1b", geometry);

		Subject testSubject;

		SubjectUtils.save(Arrays.asList(subject1));
		testSubject = SubjectUtils.getSubjectByTypeAndLabel(testSubjectType, "subject1");
		assertEquals("Subject 1a", testSubject.getName());
		assertEquals(TestFactory.FAKE_POINT_GEOMETRY, testSubject.getShape());

		SubjectUtils.save(Arrays.asList(subject2));
		testSubject = SubjectUtils.getSubjectByTypeAndLabel(testSubjectType, "subject1");
		assertEquals("Subject 1b", testSubject.getName());
		assertEquals(TestFactory.FAKE_POINT_GEOMETRY, testSubject.getShape());

		SubjectUtils.save(Arrays.asList(subject3));
		testSubject = SubjectUtils.getSubjectByTypeAndLabel(testSubjectType, "subject1");
		assertEquals("Subject 1b", testSubject.getName());
		assertEquals(geometry, testSubject.getShape());
	}

	@Test
	public void testGetSubjectByTypeAndLabel(){
		Subject subject = SubjectUtils.getSubjectByTypeAndLabel(localAuthority, "E09000001");
		
		assertEquals("E09000001", subject.getLabel());
		assertEquals("City of London", subject.getName());
	}

	@Test
	public void testGetSubjectByTypeAndName(){
		List<Subject> subject = SubjectUtils.getSubjectByTypeAndName(localAuthority, "City of London");

		assertEquals("E09000001", subject.get(0).getLabel());
		assertEquals("City of London", subject.get(0).getName());
	}

	@Test
	public void testGetSubjectByTypeAndNameIsNullWithNullTrue(){
		List<Subject> subject = SubjectUtils.getSubjectByTypeAndNameIsNull(localAuthority, true);

		assertEquals(0, subject.size());
	}

	@Test
	public void testGetSubjectByTypeAndNameIsNullWithNullFalse(){
		List<Subject> subject = SubjectUtils.getSubjectByTypeAndNameIsNull(localAuthority, false);

		assertEquals(2, subject.size());
	}
	
	@Test
	public void testGetSubjectByTypeAndLabelPatternLocalAuthorities(){
		List<Subject> localAuthorities = SubjectUtils.getSubjectByTypeAndLabelPattern(localAuthority, "%");
		
		assertEquals(2, localAuthorities.size());
	}

	@Test
	public void testGetSubjectByTypeAndNamePatternLocalAuthorities(){
		List<Subject> localAuthorities = SubjectUtils.getSubjectByTypeAndNamePattern(localAuthority, "%");

		assertEquals(2, localAuthorities.size());
	}
	
	@Test
	public void testGetSubjectByTypeAndLabelPatternLondonBoroughs(){
		String labelPattern = "E09%";
		List<Subject> londonBoroughs = SubjectUtils.getSubjectByTypeAndLabelPattern(localAuthority, labelPattern);
		
		assertEquals(1, londonBoroughs.size());
	}

	@Test
	public void testGetSubjectByTypeAndLabelPatternLondonNeighbourhoods(){
		// FIXME: Currently we cannot specify this since we do not have the notion of a parent and the lsoas do not have a label pattern
	}

	@Test
	public void testGetSubjectBySpecificationWithoutRule() throws Exception {
		DatasetRecipe spec = DataExportSpecificationBuilder.withCSVExporter().addSubjectSpecification(
				new SubjectSpecificationBuilder(AbstractONSImporter.PROVIDER.getLabel(), "localAuthority")
		).build().getDataset();
		List<Subject> subjects = SubjectUtils.getSubjectBySpecification(spec);
		assertEquals(2, subjects.size());

		for (int i=0; i < subjects.size(); i++) {
			String label = subjects.get(i).getLabel();
			assertTrue("Label " + label + " matches searched pattern E08% or E09%", label.contains("E08") || label.contains("E09"));
		}
	}

	@Test
	public void testGetSubjectBySpecificationLabelSearch() throws Exception {
		DatasetRecipe spec = DataExportSpecificationBuilder.withCSVExporter().addSubjectSpecification(
				new SubjectSpecificationBuilder(AbstractONSImporter.PROVIDER.getLabel(), "localAuthority").setMatcher("label", "E09%")
		).build().getDataset();
		List<Subject> subjects = SubjectUtils.getSubjectBySpecification(spec);
		assertTrue("Label " + subjects.get(0).getLabel() + " matches searched pattern E09%", subjects.get(0).getLabel().contains("E09"));
	}

	@Test
	public void testGetSubjectBySpecificationNameSearch() throws Exception {
		DatasetRecipe spec = DataExportSpecificationBuilder.withCSVExporter().addSubjectSpecification(
				new SubjectSpecificationBuilder(AbstractONSImporter.PROVIDER.getLabel(), "localAuthority").setMatcher("name", "%don")
		).build().getDataset();
		List<Subject> subjects = SubjectUtils.getSubjectBySpecification(spec);
		assertTrue("Name " + subjects.get(0).getName() + " matches searched pattern %don", subjects.get(0).getName().contains("don"));
	}

	@Test
	public void testGetSubjectBySpecificationWithSubject() throws Exception {
		SubjectRecipe spec = DataExportSpecificationBuilder.withCSVExporter().addSubjectSpecification(
				new SubjectSpecificationBuilder(AbstractONSImporter.PROVIDER.getLabel(), "localAuthority").setMatcher("name", "%don")
		).build().getDataset().getSubjects().get(0);
		List<Subject> subjects = SubjectUtils.getSubjectBySpecification(spec);
		assertTrue("Name " + subjects.get(0).getName() + " matches searched pattern %don", subjects.get(0).getName().contains("don"));
	}

	@Test
	public void testGetSubjectsBySpecificationWithGeoMatchRule(){
		SubjectType squareAuthority = TestFactory.makeSubjectType(TestFactory.DEFAULT_PROVIDER, "squareAuthority", "Square Authority");
		SubjectType pointSensor = TestFactory.makeSubjectType(TestFactory.DEFAULT_PROVIDER, "pointSensor", "Point Sensor");

		// Creating a square with two sensors inside
		Subject squareOne = TestFactory.makeSubject(squareAuthority, "SquareOne","Square One", TestFactory.makeSquareGeometry(0d,0d,10d));
		TestFactory.makeSubject(pointSensor, "SOSenosor01", "SOSensor01", TestFactory.makePointGeometry(1d,1d));
		TestFactory.makeSubject(pointSensor, "SOSenosor02", "SOSensor02", TestFactory.makePointGeometry(5d,5d));

		// Creating a square with one sensor inside
		Subject squareTwo = TestFactory.makeSubject(squareAuthority, "SquareTwo","Square Two", TestFactory.makeSquareGeometry(20d,20d,10d));
		TestFactory.makeSubject(pointSensor, "STSenosor01", "STSensor01", TestFactory.makePointGeometry(25d,25d));

		// Creating a sensors outside either square
		TestFactory.makeSubject(pointSensor, "NSSenosor01", "NSSensor01", TestFactory.makePointGeometry(0d,0d));
		TestFactory.makeSubject(pointSensor, "NSSenosor01", "NSSensor01", TestFactory.makePointGeometry(30d,30d));
		TestFactory.makeSubject(pointSensor, "NSSenosor01", "NSSensor01", TestFactory.makePointGeometry(100d,100d));

		// Testing sensors inside Square One
		SubjectSpecificationBuilder squareOneSpec =
				new SubjectSpecificationBuilder(TestFactory.DEFAULT_PROVIDER.getLabel(), squareAuthority.getLabel()).setMatcher("name",squareOne.getName());
		List<SubjectSpecificationBuilder> parentSpecs = new ArrayList<>();
		parentSpecs.add(squareOneSpec);
		SubjectRecipe childSpec = DataExportSpecificationBuilder.withCSVExporter().addSubjectSpecification(
				new SubjectSpecificationBuilder(TestFactory.DEFAULT_PROVIDER.getLabel(), pointSensor.getLabel()).setGeoMatcher("within", parentSpecs)
		).build().getDataset().getSubjects().get(0);

		List<Subject> subjects = SubjectUtils.getSubjectBySpecification(childSpec);

		assertEquals(2, subjects.size());

		// Testing sensors inside Square One and Two
		SubjectSpecificationBuilder squareTwoSpec =
				new SubjectSpecificationBuilder(TestFactory.DEFAULT_PROVIDER.getLabel(), squareAuthority.getLabel()).setMatcher("label",squareTwo.getLabel());
		parentSpecs.add(squareTwoSpec);
		childSpec = DataExportSpecificationBuilder.withCSVExporter().addSubjectSpecification(
				new SubjectSpecificationBuilder(TestFactory.DEFAULT_PROVIDER.getLabel(), pointSensor.getLabel()).setGeoMatcher("within", parentSpecs)
		).build().getDataset().getSubjects().get(0);

		subjects = SubjectUtils.getSubjectBySpecification(childSpec);

		assertEquals(3, subjects.size());
	}

	@Test
	public void testSubjectsContainingSubjectReturnsContainingSubject() throws Exception {
		Subject cityOfLondonLsoa = TestFactory.makeNamedSubject("E01000001");
		cityOfLondon.setShape(TestFactory.makePointGeometry(100d, 100d));
		cityOfLondonLsoa.setShape(TestFactory.makePointGeometry(100d, 100d)); // make them overlap
		SubjectUtils.save(Arrays.asList(cityOfLondon, cityOfLondonLsoa));

		List<Subject> returnedSubjects = SubjectUtils.subjectsContainingSubject(localAuthority, cityOfLondonLsoa);
		assertEquals(cityOfLondon, returnedSubjects.get(0));
		assertEquals(1, returnedSubjects.size());
	}

	@Test
	public void testSubjectsContainingSubjectReturnsNullOnWrongSubjectType() throws Exception {
		Subject cityOfLondonLsoa = TestFactory.makeNamedSubject("E01000001"); // Subject contained by 'City of London'
		List<Subject> returnedSubjects = SubjectUtils.subjectsContainingSubject(msoa, cityOfLondonLsoa);
		assertEquals(0, returnedSubjects.size());
	}

	@Test
	public void testSubjectsContainingSubjectReturnsNullOnNoContainingSubject() throws Exception {
		// We make Islington, but our fake geoms are all 0, 0 - so we move it a unit away
		Subject islingtonLsoa = TestFactory.makeNamedSubject("E01002766");
		islingtonLsoa.setShape(TestFactory.makePointGeometry(1d, 1d));
		SubjectUtils.save(Collections.singletonList(islingtonLsoa));

		List<Subject> returnedSubjects = SubjectUtils.subjectsContainingSubject(localAuthority, islingtonLsoa);
		assertEquals(0, returnedSubjects.size());
	}

	@Test
	public void testSubjectsWithinSubjectReturnsContainingSubject() throws Exception {
		Subject cityOfLondonLsoa = TestFactory.makeNamedSubject("E01000001");
		cityOfLondon.setShape(TestFactory.makePointGeometry(100d, 100d));
		cityOfLondonLsoa.setShape(TestFactory.makePointGeometry(100d, 100d)); // make them overlap
		SubjectUtils.save(Arrays.asList(cityOfLondon, cityOfLondonLsoa));

		List<Subject> returnedSubjects = SubjectUtils.subjectsWithinSubject(localAuthority, cityOfLondonLsoa);
		assertEquals(cityOfLondon, returnedSubjects.get(0));
		assertEquals(1, returnedSubjects.size());
	}

	@Test
	public void testSubjectsWithinSubjectReturnsNullOnWrongSubjectType() throws Exception {
		Subject cityOfLondonLsoa = TestFactory.makeNamedSubject("E01000001"); // Subject contained by 'City of London'
		List<Subject> returnedSubjects = SubjectUtils.subjectsWithinSubject(msoa, cityOfLondonLsoa);
		assertEquals(0, returnedSubjects.size());
	}

	@Test
	public void testSubjectsWithinSubjectReturnsEmptyOnNoContainingSubject() throws Exception {
		// We make Islington, but our fake geoms are all 0, 0 - so we move it a unit away
		Subject islingtonLsoa = TestFactory.makeNamedSubject("E01002766");
		islingtonLsoa.setShape(TestFactory.makePointGeometry(1d, 1d));
		SubjectUtils.save(Collections.singletonList(islingtonLsoa));

		List<Subject> returnedSubjects = SubjectUtils.subjectsWithinSubject(localAuthority, islingtonLsoa);
		assertEquals(0, returnedSubjects.size());
	}

	@Test
	public void testSubjectNearestSubjectReturnsNearestSubject() throws Exception {
		Subject islingtonLsoa = TestFactory.makeNamedSubject("E01002766");
		Subject cityOfLondonLsoa = TestFactory.makeNamedSubject("E01000001");
		cityOfLondon.setShape(TestFactory.makePointGeometry(100d, 100d));
		islingtonLsoa.setShape(TestFactory.makePointGeometry(100.005d, 100d)); // make this one further
		cityOfLondonLsoa.setShape(TestFactory.makePointGeometry(100.002d, 100d)); // make this one nearer
		SubjectUtils.save(Arrays.asList(cityOfLondon, cityOfLondonLsoa, islingtonLsoa));

		Subject returnedSubject = SubjectUtils.subjectNearestSubject(lsoa, cityOfLondon, 0.01d);
		assertEquals(cityOfLondonLsoa, returnedSubject);
	}

	@Test
	public void testSubjectNearestSubjectReturnsOnlySubjectsWithinRadius() throws Exception {
		Subject islingtonLsoa = TestFactory.makeNamedSubject("E01002766");
		cityOfLondon.setShape(TestFactory.makePointGeometry(100d, 100d));
		islingtonLsoa.setShape(TestFactory.makePointGeometry(100.1d, 100d)); // make this one outside the radius
		SubjectUtils.save(Arrays.asList(cityOfLondon, islingtonLsoa));
		Subject returnedSubject = SubjectUtils.subjectNearestSubject(lsoa, cityOfLondon, 0.01d);
		assertNull(returnedSubject);
	}

	@Test
	public void testSubjectNearestSubjectReturnsNullOnWrongSubjectType() throws Exception {
		Subject islingtonLsoa = TestFactory.makeNamedSubject("E01002766");
		cityOfLondon.setShape(TestFactory.makePointGeometry(100d, 100d));
		islingtonLsoa.setShape(TestFactory.makePointGeometry(100d, 100d)); // make them very nearby
		SubjectUtils.save(Arrays.asList(cityOfLondon, islingtonLsoa));

		Subject returnedSubject = SubjectUtils.subjectNearestSubject(msoa, cityOfLondon, 1d);
		assertNull(returnedSubject);
	}

	@Test @Ignore
	public  void testFindSRID() {
		assertEquals(4326, SubjectUtils.findSRID());
	}
}
