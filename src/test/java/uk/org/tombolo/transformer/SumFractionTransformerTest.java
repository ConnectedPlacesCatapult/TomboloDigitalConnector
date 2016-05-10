package uk.org.tombolo.transformer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Geography;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.TimedValueUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SumFractionTransformerTest {
	
	SumFractionTransformer transformer = new SumFractionTransformer();
	
	@Test
	public void testTransform(){		
		Attribute threeYearOlds = new Attribute();
		threeYearOlds.setId(1);
		Attribute fourYearOlds = new Attribute();
		fourYearOlds.setId(2);
		Attribute fiveYearOlds = new Attribute();
		fiveYearOlds.setId(3);
		Attribute everybody = new Attribute();
		everybody.setId(4);
		Attribute children = new Attribute();
		children.setId(5);
		
		LocalDateTime t1 = LocalDateTime.now();
		LocalDateTime t2 = LocalDateTime.now();
		LocalDateTime t3 = LocalDateTime.now();

		Geography place1 = new Geography();
		place1.setId(1);
		Geography place2 = new Geography();
		place2.setId(2);
		Geography place3 = new Geography();
		place3.setId(3);

		List<Geography> geographies = new ArrayList<Geography>();
		geographies.add(place1);
		geographies.add(place2);
		geographies.add(place3);

		List<Attribute> inputAttributes = new ArrayList<Attribute>();
		inputAttributes.add(threeYearOlds);
		inputAttributes.add(fourYearOlds);
		inputAttributes.add(fiveYearOlds);
		inputAttributes.add(everybody);

		TimedValue threePlace1T2 = new TimedValue(place1, everybody, t2, 2d);
		TimedValue fourPlace1T2 = new TimedValue(place1, everybody, t2, 1d);
		TimedValue fivePlace1T2 = new TimedValue(place1, everybody, t2, 2d);
		TimedValue everybodyPlace1T2 = new TimedValue(place1, everybody, t2, 10d);

		TimedValue threePlace2T1 = new TimedValue(place2, everybody, t1, 1d);
		TimedValue fourPlace2T1 = new TimedValue(place2, everybody, t1, 1d);
		TimedValue fivePlace2T1 = new TimedValue(place2, everybody, t1, 2d);
		TimedValue everybodyPlace2T2 = new TimedValue(place2, everybody, t2, 10d);

		TimedValue fourPlace3T1 = new TimedValue(place2, everybody, t1, 1d);
		TimedValue fivePlace3T3 = new TimedValue(place2, everybody, t3, 2d);
		TimedValue everybodyPlace3T2 = new TimedValue(place2, everybody, t2, 10d);



		List<TimedValue> latestEverybodyPlace1 = new ArrayList<TimedValue>();
		latestEverybodyPlace1.add(everybodyPlace1T2);

		TimedValueUtils utils = mock(TimedValueUtils.class);
		// Place 1
		when(utils.getLatestByGeographyAndAttribute(place1, threeYearOlds)).thenReturn(Optional.of(threePlace1T2));
		when(utils.getLatestByGeographyAndAttribute(place1, fourYearOlds)).thenReturn(Optional.of(fourPlace1T2));
		when(utils.getLatestByGeographyAndAttribute(place1, fiveYearOlds)).thenReturn(Optional.of(fivePlace1T2));
		when(utils.getLatestByGeographyAndAttribute(place1, everybody)).thenReturn(Optional.of(everybodyPlace1T2));
		// Place 2
		when(utils.getLatestByGeographyAndAttribute(place2, threeYearOlds)).thenReturn(Optional.of(threePlace2T1));
		when(utils.getLatestByGeographyAndAttribute(place2, fourYearOlds)).thenReturn(Optional.of(fourPlace2T1));
		when(utils.getLatestByGeographyAndAttribute(place2, fiveYearOlds)).thenReturn(Optional.of(fivePlace2T1));
		when(utils.getLatestByGeographyAndAttribute(place2, everybody)).thenReturn(Optional.of(everybodyPlace2T2));
		// Place 3
		when(utils.getLatestByGeographyAndAttribute(place3, threeYearOlds)).thenReturn(Optional.empty());
		when(utils.getLatestByGeographyAndAttribute(place3, fourYearOlds)).thenReturn(Optional.of(fourPlace3T1));
		when(utils.getLatestByGeographyAndAttribute(place3, fiveYearOlds)).thenReturn(Optional.of(fivePlace3T3));
		when(utils.getLatestByGeographyAndAttribute(place3, everybody)).thenReturn(Optional.of(everybodyPlace3T2));
		transformer.setTimedValueUtils(utils);


		List<TimedValue> values = transformer.transform(geographies, inputAttributes, children);

		// Two places
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
	
}
