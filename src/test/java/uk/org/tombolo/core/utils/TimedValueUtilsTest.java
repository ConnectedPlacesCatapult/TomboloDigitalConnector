package uk.org.tombolo.core.utils;

import org.junit.Test;
import uk.org.tombolo.AbstractTest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TimedValueUtilsTest extends AbstractTest {

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
}
