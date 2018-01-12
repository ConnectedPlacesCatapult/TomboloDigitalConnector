package uk.org.tombolo.core.utils;

import org.hibernate.NonUniqueObjectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.importer.ParsingException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TimedValueUtils {
	static Logger log = LoggerFactory.getLogger(TimedValueUtils.class);

	public static List<TimedValue> getBySubjectAndAttribute(Subject subject, Attribute attribute){
		return HibernateUtil.withSession((session) -> {
			return session.createQuery("from TimedValue where id.subject = :subject and id.attribute = :attribute", TimedValue.class)
					.setParameter("subject", subject)
					.setParameter("attribute", attribute)
					.setCacheable(true)
					.list();
		});
	}

	public static TimedValue getLatestBySubjectAndAttribute(Subject subject, Attribute attribute) {
		return HibernateUtil.withSession((session) -> {
			return session.createQuery("from TimedValue where id.subject = :subject and id.attribute = :attribute order by id.timestamp desc", TimedValue.class)
					.setParameter("subject", subject)
					.setParameter("attribute", attribute)
					.setMaxResults(1)
					.setCacheable(true)
					.uniqueResult();
		});
	}

	/**
	 * getLatestBySubjectAndAttributes
	 * Returns a list of TimedValues with the latest timestamp for each attribute on a subject
	 *
	 * This is here for optimisation reasons. In short:
	 *
	 *   * Calling getLatestBySubjectAndAttribute for each subject/attribute pair individually
	 *     is very expensive (this impl is much, much faster)
	 *   * The fastest way is to use a SELECT DISTINCT ON postgres query, but we can't do that
	 *     because Hibernate doesn't support DISTINCT ON.
	 *   * So instead we get a list of every TimedValue for every Attribute and sort
	 *     through them in Java to find the latest.
	 *
	 * @param subject The Subject to retrieve the values for
	 * @param attributes A list of attributes to return the values on
	 * @return A list of the latest TimedValues for each subject/attribute pair
	 */
	public static List<TimedValue> getLatestBySubjectAndAttributes(Subject subject, List<Attribute> attributes) {
		return HibernateUtil.withSession((session) -> {
			List<TimedValue> results = session.createQuery("from TimedValue where id.subject = :subject and id.attribute in :attributes", TimedValue.class)
					.setParameter("subject", subject)
					.setParameter("attributes", attributes)
					.setCacheable(true)
					.list();

			// We use stream collection to build a map of Attribute -> TimedValue while
			// discarding duplicates that have older timestamps. In this manner we build
			// a map with only the latest timestamped TimedValues for each Attribute and
			// discard the others.
			Map<Attribute, TimedValue> tv = results.stream().collect(Collectors.toMap(
					timedValue -> {
						return timedValue.getId().getAttribute();
					},
					Function.identity(),
					(t1, t2) -> {
						if (t1.getId().getTimestamp().isAfter(t2.getId().getTimestamp())) {
							return t1;
						} else {
							return t2;
						}
					}
			));

			// Then we discard the keys and return the values. Voila!
			return new ArrayList<>(tv.values());
		});
	}
	
	public static void save(TimedValue timedValue){
		save(Collections.singletonList(timedValue));
	}

	public static int save(List<TimedValue> timedValues){
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
							timedValue.getId().getSubject().getLabel(),
							timedValue.getId().getAttribute().getDescription(),
							timedValue.getId().getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
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
	public static LocalDateTime parseTimestampString(String timestampString) throws ParsingException {
		// Check if well formed to the second
		if (timestampString.matches("^\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d$"))
			return LocalDateTime.parse(timestampString);

		// Check if is year format
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

		// Check if Mon-yr format that is occasionally used by ONS
		if (timestampString.matches("^\\w\\w\\w-\\d\\d")) {
			return parse(timestampString,"MMM-yy");
			// Check if "Month year" format that is occasionally used by ONS claimants
		} else if (timestampString.matches("(^|\\s)" +
				"(January|February|March|April|May|June|July|August|September|October|November|December)" +
				"\\s(19|20)\\d\\d?")) {
			return parse(timestampString, "MMMM yyyy");
		}

		// Neither well formed to the second nor year
		throw new ParsingException("Unparsable timestamp: " + timestampString);
	}

	/**
	 * Parses the timestamp string given the pattern.
	 *
	 * @param timestampString input timestamp string
	 * @param pattern time pattern
	 * @return last day of the month given in the string
	 */
	private static LocalDateTime parse(String timestampString, String pattern) {
		DateTimeFormatter formatter = new DateTimeFormatterBuilder()
				.appendPattern(pattern)
				.parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
				.parseDefaulting(ChronoField.HOUR_OF_DAY, 23)
				.parseDefaulting(ChronoField.MINUTE_OF_HOUR, 59)
				.parseDefaulting(ChronoField.SECOND_OF_MINUTE, 59)
				.toFormatter();
		return LocalDateTime.parse(timestampString, formatter)
				.with(TemporalAdjusters.lastDayOfMonth());
	}
}
