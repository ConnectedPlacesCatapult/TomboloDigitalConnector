package uk.org.tombolo.core.utils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.hibernate.Criteria;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Geography;
import uk.org.tombolo.core.TimedValue;

public class TimedValueUtils {
	static Logger log = LoggerFactory.getLogger(TimedValueUtils.class);
	
	public static List<TimedValue> getByGeographyAndAttribute(Geography geography, Attribute attribute){
		return withSession((session) -> {
			Criteria criteria = session.createCriteria(TimedValue.class);
			criteria = criteria.add(Restrictions.eq("id.geography", geography));
			criteria = criteria.add(Restrictions.eq("id.attribute", attribute));

			// FIXME: This should be paginated
			return (List<TimedValue>) criteria.list();
		});
	}

	public static Optional<TimedValue> getLatestByGeographyAndAttribute(Geography geography, Attribute attribute) {
		return withSession((session) -> {
			Criteria criteria = session.createCriteria(TimedValue.class);
			criteria = criteria.add(Restrictions.eq("id.geography", geography));
			criteria = criteria.add(Restrictions.eq("id.attribute", attribute));
			criteria = criteria.addOrder(Order.desc("id.timestamp"));
			criteria.setMaxResults(1);

			if (criteria.list().isEmpty()) {
				return Optional.empty();
			} else {
				return Optional.of((TimedValue) criteria.list().get(0));
			}
		});
	}
	
	public static Integer save(TimedValue timedValue){
		return withSession((session) -> {
			session.beginTransaction();
			try{
				session.saveOrUpdate(timedValue);
			}catch(NonUniqueObjectException e){
				//FIXME: Find out why this is happening
				log.info("Could not save timed value for geography {}, attribute {}, time {}: {}",
						timedValue.getId().getGeography().getLabel(),
						timedValue.getId().getAttribute().getName(),
						timedValue.getId().getTimestamp().toString(),
						e.getMessage());
			}
			session.getTransaction().commit();
			return 1;
		});
	}

	public int save(List<TimedValue> timedValues){
		return withSession((session) -> {
			int saved = 0;
			session.beginTransaction();
			for (TimedValue timedValue : timedValues){
				try{
					session.saveOrUpdate(timedValue);
					saved++;
				}catch(NonUniqueObjectException e){
					//FIXME: Find out why this is happening
					log.info("Could not save timed value for geography {}, attribute {}, time {}: {}",
							timedValue.getId().getGeography().getLabel(),
							timedValue.getId().getAttribute().getName(),
							timedValue.getId().getTimestamp().toString(),
							e.getMessage());
				}
				if ( saved % 20 == 0 ) { //20, same as the JDBC batch size
					//flush a batch of inserts and release memory:
					session.flush();
					session.clear();
				}
			}
			session.getTransaction().commit();
			return saved;
		});
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

	private static <T> T withSession(Function<Session, T> fn) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		try {
			return fn.apply(session);
		} finally {
			session.close();
		}
	}
}
