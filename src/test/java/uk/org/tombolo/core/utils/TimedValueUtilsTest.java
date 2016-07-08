package uk.org.tombolo.core.utils;

import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValue;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

	@Test
	public void testGetLatestBySubjectAndAttributes() {
		Subject subject = TestFactory.makeNamedSubject("E01000001");
		Attribute attribute1 = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr1");
		Attribute attribute2 = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr2");

		TestFactory.makeTimedValue("E01000001", attribute1, "2011-01-03T00:00", 100d);
		TimedValue latest1 = TestFactory.makeTimedValue("E01000001", attribute1, "2011-01-05T00:00", 200d);
		TestFactory.makeTimedValue("E01000001", attribute1, "2011-01-03T00:00", 300d);

		TestFactory.makeTimedValue("E01000001", attribute2, "2011-01-02T00:00", 400d);
		TestFactory.makeTimedValue("E01000001", attribute2, "2011-01-01T00:00", 400d);
		TimedValue latest2 = TestFactory.makeTimedValue("E01000001", attribute2, "2011-01-03T00:00", 400d);


		List<TimedValue> results = TimedValueUtils.getLatestBySubjectAndAttributes(subject, Arrays.asList(attribute1, attribute2));

		assertTrue("Contains value for attr1 with latest timestamp", results.contains(latest1));
		assertTrue("Contains value for attr2 with latest timestamp", results.contains(latest2));
	}
}
