package uk.org.tombolo.core;

import static org.junit.Assert.*;

import java.time.LocalDateTime;


import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Test;

import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.GeographyUtils;
import uk.org.tombolo.core.utils.HibernateUtil;

public class TimedValueTest {

	@Test
	public void testSave(){
		Session session = HibernateUtil.getSessionFactory().openSession();
		
		Transaction transaction = session.beginTransaction();
		
		Geography geography = GeographyUtils.getTestGeography();
				
		Attribute attribute = AttributeUtils.getTestAttribute();
				
		LocalDateTime timestamp = LocalDateTime.now();
		
		TimedValue tv = new TimedValue(geography, attribute, timestamp, 15.7d);
		session.save(tv);
				
		TimedValue testTv = (TimedValue)session.load(TimedValue.class, tv.getId());
		assertEquals(15.7d, testTv.getValue(), 0.1d);
		
		transaction.rollback();
	}
	
}
