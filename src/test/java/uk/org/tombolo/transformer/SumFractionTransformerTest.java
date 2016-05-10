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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SumFractionTransformerTest {
	
	SumFractionTransformer transformer = new SumFractionTransformer();
	
	@Test
	public void testTransform(){		
		Attribute threeYearOlds = new Attribute();
		Attribute fourYearOlds = new Attribute();
		Attribute fiveYearOlds = new Attribute();
		Attribute everybody = new Attribute();
		Attribute children = new Attribute();
		
		LocalDateTime t1 = LocalDateTime.now();
		LocalDateTime t2 = LocalDateTime.now();
		
		Geography place1 = new Geography();
		Geography place2 = new Geography();

		List<Geography> geographies = new ArrayList<Geography>();
		geographies.add(place1);
		geographies.add(place2);

		List<Attribute> inputAttributes = new ArrayList<Attribute>();
		inputAttributes.add(threeYearOlds);
		inputAttributes.add(fourYearOlds);
		inputAttributes.add(fiveYearOlds);
		inputAttributes.add(everybody);

		TimedValue threePlace1T2 = new TimedValue(place1, everybody, t2, 2d);
		TimedValue fourPlace1T2 = new TimedValue(place1, everybody, t2, 1d);
		TimedValue fivePlace1T2 = new TimedValue(place1, everybody, t2, 2d);
		TimedValue everybodyPlace1T2 = new TimedValue(place1, everybody, t2, 10d);
		

		List<TimedValue> latestEverybodyPlace1 = new ArrayList<TimedValue>();
		latestEverybodyPlace1.add(everybodyPlace1T2);

		TimedValueUtils utils = mock(TimedValueUtils.class);
		when(utils.getLatestByGeographyAndAttribute(place1, everybody)).thenReturn(Optional.of(everybodyPlace1T2));
		transformer.setTimedValueUtils(utils);


		List<TimedValue> values = transformer.transform(geographies, inputAttributes, children);

		assertEquals(2, values);
	}
	
}
