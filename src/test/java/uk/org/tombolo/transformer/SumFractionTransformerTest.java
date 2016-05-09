package uk.org.tombolo.transformer;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.Test;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Geography;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.TimedValueUtils;

public class SumFractionTransformerTest {
	
	SumFractionTransformer transformer = new SumFractionTransformer();
	
	@Test
	public void testTransform(){		
		Attribute threeYearOlds = new Attribute();
		Attribute fourYearOlds = new Attribute();
		Attribute fiveYearOlds = new Attribute();
		Attribute everybody = new Attribute();
		
		LocalDateTime t1 = LocalDateTime.now();
		LocalDateTime t2 = LocalDateTime.now();
		
		Geography place1 = new Geography();
		Geography place2 = new Geography();
		
		TimedValue threePlace1T2 = new TimedValue(place1, everybody, t2, 2d);
		TimedValue fourPlace1T2 = new TimedValue(place1, everybody, t2, 1d);
		TimedValue fivePlace1T2 = new TimedValue(place1, everybody, t2, 2d);
		TimedValue everybodyPlace1T2 = new TimedValue(place1, everybody, t2, 10d);
		
		TimedValueUtils utils = mock(TimedValueUtils.class);
		
		when(utils.getLatestByGeographyAndAttribute(place1, everybody)).thenReturn(List<bla>);
		transforer.setTimedValueUtils(utils);
				
		List<TimedValue> values = transformer.transform(null, null);
		
	}
	
}
