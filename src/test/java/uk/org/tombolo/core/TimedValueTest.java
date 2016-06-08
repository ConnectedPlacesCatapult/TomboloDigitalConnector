package uk.org.tombolo.core;

import org.hibernate.Transaction;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.HibernateUtil;
import uk.org.tombolo.core.utils.SubjectUtils;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

public class TimedValueTest extends AbstractTest {

	@Test
	public void testSave(){
		HibernateUtil.withSession(session -> {
			Transaction transaction = session.beginTransaction();

			Subject subject = SubjectUtils.getTestSubject();

			Attribute attribute = AttributeUtils.getTestAttribute();

			LocalDateTime timestamp = LocalDateTime.now();

			TimedValue tv = new TimedValue(subject, attribute, timestamp, 15.7d);
			session.save(tv);

			TimedValue testTv = (TimedValue)session.load(TimedValue.class, tv.getId());
			assertEquals(15.7d, testTv.getValue(), 0.1d);

			transaction.rollback();
		});
	}
	
}
