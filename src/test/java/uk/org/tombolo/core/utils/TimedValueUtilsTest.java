package uk.org.tombolo.core.utils;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.geotools.geometry.GeometryBuilder;
import org.junit.Test;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Geography;
import uk.org.tombolo.core.TimedValue;

public class TimedValueUtilsTest {

	@Test
	public void testParseTimestampString(){
		Map<String, LocalDateTime> testCases = new HashMap<String, LocalDateTime>();
		
		testCases.put("2013", LocalDateTime.parse("2013-12-31T23:59:59"));
		testCases.put("2013 - 15", LocalDateTime.parse("2015-12-31T23:59:59"));
		testCases.put("2014/15", LocalDateTime.parse("2015-12-31T23:59:59"));
		testCases.put("bla", null);
		
		for (String testCase : testCases.keySet()){
			assertEquals(testCase, testCases.get(testCase), TimedValueUtils.parseTimestampString(testCase));
		}
	}

	@Test
	public void testGetLatestByGeographyAndAttribute() {
		Geography geography = GeographyUtils.getTestGeography();
		Attribute attribute = AttributeUtils.getTestAttribute();

		System.out.println(geography.getId());
		Optional<TimedValue> value1 = TimedValueUtils.getLatestByGeographyAndAttribute(geography, attribute);
		assertEquals(false, value1.isPresent());

		attribute.setId(10); // This badly needs mocking
		Optional<TimedValue> value2 = TimedValueUtils.getLatestByGeographyAndAttribute(geography, attribute);
		assertEquals(true, value2.isPresent());
	}
}
