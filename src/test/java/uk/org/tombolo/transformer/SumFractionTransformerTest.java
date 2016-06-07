package uk.org.tombolo.transformer;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.TimedValueUtils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SumFractionTransformerTest extends AbstractTest {

	// Input attributes with population count for different ages
	Attribute threeYearOlds = new Attribute();
	Attribute fourYearOlds = new Attribute();
	Attribute fiveYearOlds = new Attribute();
	// Input attribute with population count of the entire population
	Attribute everybody = new Attribute();
	// Output attribute with fraction of children among the total population (3yo + 4yo + 5yo)/everybody
	Attribute children = new Attribute();

	// Three accending timepoints
	LocalDateTime t1 = LocalDateTime.now();
	LocalDateTime t2 = LocalDateTime.now();
	LocalDateTime t3 = LocalDateTime.now();

	// Three greographies we would like to use
	Subject place1 = new Subject();
	Subject place2 = new Subject();
	Subject place3 = new Subject();

	@Before
	public void setUp(){
		threeYearOlds.setId(1);
		fourYearOlds.setId(2);
		fiveYearOlds.setId(3);
		everybody.setId(4);
		children.setId(5);

		place1.setId(1);
		place2.setId(2);
		place3.setId(3);

	}

	SumFractionTransformer transformer = new SumFractionTransformer();
	
	@Test
	public void testTransform() {
		// FIXME: Consider breaking up into multiple tests

		List<Subject> geographies = Arrays.asList(place1,place2,place3);

		List<Attribute> inputAttributes = Arrays.asList(threeYearOlds,fourYearOlds,fiveYearOlds,everybody);

		// Place with all latest data at same timepoint
		TimedValue threePlace1T2 = new TimedValue(place1, everybody, t2, 2d);
		TimedValue fourPlace1T2 = new TimedValue(place1, everybody, t2, 1d);
		TimedValue fivePlace1T2 = new TimedValue(place1, everybody, t2, 2d);
		TimedValue everybodyPlace1T2 = new TimedValue(place1, everybody, t2, 10d);

		// Place with dividing attribute at later time
		TimedValue threePlace2T1 = new TimedValue(place2, everybody, t1, 1d);
		TimedValue fourPlace2T1 = new TimedValue(place2, everybody, t1, 1d);
		TimedValue fivePlace2T1 = new TimedValue(place2, everybody, t1, 2d);
		TimedValue everybodyPlace2T2 = new TimedValue(place2, everybody, t2, 10d);

		// Place with missing value for attribute 1 and other attributes at different timepoints
		TimedValue fourPlace3T1 = new TimedValue(place2, everybody, t1, 1d);
		TimedValue fivePlace3T3 = new TimedValue(place2, everybody, t3, 2d);
		TimedValue everybodyPlace3T2 = new TimedValue(place2, everybody, t2, 10d);

		TimedValueUtils utils = mock(TimedValueUtils.class);
		// Place 1
		when(utils.getLatestBySubjectAndAttribute(place1, threeYearOlds)).thenReturn(Optional.of(threePlace1T2));
		when(utils.getLatestBySubjectAndAttribute(place1, fourYearOlds)).thenReturn(Optional.of(fourPlace1T2));
		when(utils.getLatestBySubjectAndAttribute(place1, fiveYearOlds)).thenReturn(Optional.of(fivePlace1T2));
		when(utils.getLatestBySubjectAndAttribute(place1, everybody)).thenReturn(Optional.of(everybodyPlace1T2));
		// Place 2
		when(utils.getLatestBySubjectAndAttribute(place2, threeYearOlds)).thenReturn(Optional.of(threePlace2T1));
		when(utils.getLatestBySubjectAndAttribute(place2, fourYearOlds)).thenReturn(Optional.of(fourPlace2T1));
		when(utils.getLatestBySubjectAndAttribute(place2, fiveYearOlds)).thenReturn(Optional.of(fivePlace2T1));
		when(utils.getLatestBySubjectAndAttribute(place2, everybody)).thenReturn(Optional.of(everybodyPlace2T2));
		// Place 3
		when(utils.getLatestBySubjectAndAttribute(place3, threeYearOlds)).thenReturn(Optional.empty());
		when(utils.getLatestBySubjectAndAttribute(place3, fourYearOlds)).thenReturn(Optional.of(fourPlace3T1));
		when(utils.getLatestBySubjectAndAttribute(place3, fiveYearOlds)).thenReturn(Optional.of(fivePlace3T3));
		when(utils.getLatestBySubjectAndAttribute(place3, everybody)).thenReturn(Optional.of(everybodyPlace3T2));
		transformer.setTimedValueUtils(utils);


		List<TimedValue> values = transformer.transform(geographies, inputAttributes, children);

		// Three places
		assertEquals(3, values.size());

		// Place 1
		assertEquals(place1, values.get(0).getId().getGeography());
		assertEquals(t2, values.get(0).getId().getTimestamp());
		assertEquals(children, values.get(0).getId().getAttribute());
		assertEquals(0.5d, values.get(0).getValue(), 0.001d);

		// Place 2
		assertEquals(place2, values.get(1).getId().getGeography());
		assertEquals(t2, values.get(1).getId().getTimestamp());
		assertEquals(children, values.get(0).getId().getAttribute());
		assertEquals(0.4d, values.get(1).getValue(), 0.001d);

		// Place 3
		assertEquals(place3, values.get(2).getId().getGeography());
		assertEquals(t3, values.get(2).getId().getTimestamp());
		assertEquals(children, values.get(0).getId().getAttribute());
		assertEquals(0.3d, values.get(2).getValue(), 0.001d);
	}

	@Test
	public void testTransformNonExisting() {
		TimedValueUtils utils = mock(TimedValueUtils.class);
		when(utils.getLatestBySubjectAndAttribute(any(Subject.class), any(Attribute.class)))
				.thenReturn(Optional.empty());
		transformer.setTimedValueUtils(utils);

		List<Subject> geographies = Arrays.asList(place1);

		List<Attribute> inputAttributes = Arrays.asList(threeYearOlds,fourYearOlds,everybody);

		List<TimedValue> values = transformer.transform(geographies, inputAttributes, children);

		assertEquals(0,values.size());
	}

	@Test
	public void testTransformNonExistingDenominator() {
		TimedValueUtils utils = mock(TimedValueUtils.class);
		when(utils.getLatestBySubjectAndAttribute(place1, threeYearOlds))
				.thenReturn(Optional.of(new TimedValue(place1, threeYearOlds, t1, 3.0d)));
		when(utils.getLatestBySubjectAndAttribute(place1, fourYearOlds))
				.thenReturn(Optional.of(new TimedValue(place1, fourYearOlds, t1, 4.0d)));
		when(utils.getLatestBySubjectAndAttribute(place1, everybody))
				.thenReturn(Optional.empty());
		transformer.setTimedValueUtils(utils);

		List<Subject> geographies = Arrays.asList(place1);

		List<Attribute> inputAttributes = Arrays.asList(threeYearOlds,fourYearOlds,everybody);

		List<TimedValue> values = transformer.transform(geographies, inputAttributes, children);

		assertEquals(0,values.size());
	}

	@Test
	public void testTransformNonExistingNumerator() {
		TimedValueUtils utils = mock(TimedValueUtils.class);
		when(utils.getLatestBySubjectAndAttribute(place1, threeYearOlds))
				.thenReturn(Optional.of(new TimedValue(place1, threeYearOlds, t1, 3.0d)));
		when(utils.getLatestBySubjectAndAttribute(place1, fourYearOlds))
				.thenReturn(Optional.empty());
		when(utils.getLatestBySubjectAndAttribute(place1, everybody))
				.thenReturn(Optional.of(new TimedValue(place1, fourYearOlds, t1, 4.0d)));
		transformer.setTimedValueUtils(utils);

		List<Subject> geographies = Arrays.asList(place1);

		List<Attribute> inputAttributes = Arrays.asList(threeYearOlds,fourYearOlds,everybody);

		List<TimedValue> values = transformer.transform(geographies, inputAttributes, children);

		// This is controversial
		// We could decide to return no value since one of the numerator values was missing
		assertEquals(1,values.size());
		assertEquals(.75d, values.get(0).getValue(),0.001d);
	}

	@Test
	public void testTransformNonExistingNumerators() {
		TimedValueUtils utils = mock(TimedValueUtils.class);
		when(utils.getLatestBySubjectAndAttribute(place1, threeYearOlds))
				.thenReturn(Optional.empty());
		when(utils.getLatestBySubjectAndAttribute(place1, fourYearOlds))
				.thenReturn(Optional.empty());
		when(utils.getLatestBySubjectAndAttribute(place1, everybody))
				.thenReturn(Optional.of(new TimedValue(place1, fourYearOlds, t1, 4.0d)));
		transformer.setTimedValueUtils(utils);

		List<Subject> geographies = Arrays.asList(place1);

		List<Attribute> inputAttributes = Arrays.asList(threeYearOlds,fourYearOlds,everybody);

		List<TimedValue> values = transformer.transform(geographies, inputAttributes, children);

		// Both numerators are missing
		assertEquals(0,values.size());
	}

	// FIXME: Add more corner cases, especially for missing values
}
