package uk.org.tombolo.core;

import org.hibernate.Transaction;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.HibernateUtil;
import uk.org.tombolo.core.utils.SubjectUtils;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

public class TimedValueTest extends AbstractTest {
	@Test
	public void testSave(){
		TimedValue timedValue = TestFactory.makeTimedValue(
				TestFactory.makeNamedSubject("E01000001").getLabel(),
				TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr_label"),
				TestFactory.TIMESTAMP,
				15.7d
		);

		assertEquals(15.7d, timedValue.getValue(), 0.1d);
	}
	
}
