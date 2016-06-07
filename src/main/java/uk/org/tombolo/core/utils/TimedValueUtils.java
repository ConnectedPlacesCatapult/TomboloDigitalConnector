package uk.org.tombolo.core.utils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.hibernate.Criteria;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValue;

public class TimedValueUtils {
	static Logger log = LoggerFactory.getLogger(TimedValueUtils.class);
	
	public List<TimedValue> getBySubjectAndAttribute(Subject subject, Attribute attribute){
		return HibernateUtil.withSession((session) -> {
			Criteria criteria = session.createCriteria(TimedValue.class);
			criteria = criteria.add(Restrictions.eq("id.geography", subject));
			criteria = criteria.add(Restrictions.eq("id.attribute", attribute));

			// FIXME: This should be paginated
			return (List<TimedValue>) criteria.list();
		});
	}

	public Optional<TimedValue> getLatestBySubjectAndAttribute(Subject subject, Attribute attribute) {
		return HibernateUtil.withSession((session) -> {
			Criteria criteria = session.createCriteria(TimedValue.class);
			criteria = criteria.add(Restrictions.eq("id.geography", subject));
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
	
	public void save(TimedValue timedValue){
		save(Arrays.asList(timedValue));
	}

	public int save(List<TimedValue> timedValues){
		return HibernateUtil.withSession((session) -> {
			int saved = 0;
			session.beginTransaction();
			for (TimedValue timedValue : timedValues){
				try{
					session.saveOrUpdate(timedValue);
					saved++;
				}catch(NonUniqueObjectException e){
					// This is happening because the TFL stations contain a duplicate ID
					log.warn("Could not save timed value for subject {}, attribute {}, time {}: {}",
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
}
