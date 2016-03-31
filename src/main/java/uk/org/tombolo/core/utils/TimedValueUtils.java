package uk.org.tombolo.core.utils;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Geography;
import uk.org.tombolo.core.TimedValue;

public class TimedValueUtils {

	static Session session = HibernateUtil.getSessionFactory().openSession();

	public static List<TimedValue> getByGeographyAndAttribute(Geography geography, Attribute attribute){
		Criteria criteria = session.createCriteria(TimedValue.class);
		criteria = criteria.add(Restrictions.eq("id.geography", geography));
		criteria = criteria.add(Restrictions.eq("id.attribute", attribute));
		
		// FIXME: This should be paginated
		return (List<TimedValue>) criteria.list();
	}
	
	public static void save(TimedValue timedValue){
		session.beginTransaction();
		try{
			session.saveOrUpdate(timedValue);
		}catch(NonUniqueObjectException e){
			//FIXME: Find out why this is happening
		}
		session.getTransaction().commit();
	}

	public static void save(List<TimedValue> timedValues){
		session.beginTransaction();
		try{
			for (TimedValue timedValue : timedValues){
				session.saveOrUpdate(timedValue);
			}
		}catch(NonUniqueObjectException e){
			//FIXME: Find out why this is happening
		}
		session.getTransaction().commit();
	}

	
	/**
	 * FIXME: Supports a very limited number of strings (implemented on-demand)
	 * 
	 * @param timestampString
	 * @return
	 */
	public static LocalDateTime parseTimestampString(String timestampString){
		String endOfYear = "-12-31T23:59:59";
		
		if (timestampString.matches("^\\d\\d\\d\\d$")){
			return LocalDateTime.parse(timestampString+endOfYear);
		}else if (timestampString.matches("^\\d\\d\\d\\d - \\d\\d$")){
			String year = timestampString.substring(0,2)+timestampString.substring(timestampString.length()-2, timestampString.length());
			return LocalDateTime.parse(year+endOfYear);
		}else if (timestampString.matches("^\\d\\d\\d\\d\\/\\d\\d$")){
			String year = timestampString.substring(0,2)+timestampString.substring(timestampString.length()-2, timestampString.length());
			return LocalDateTime.parse(year+endOfYear);
		}

		return null;
	}
}
