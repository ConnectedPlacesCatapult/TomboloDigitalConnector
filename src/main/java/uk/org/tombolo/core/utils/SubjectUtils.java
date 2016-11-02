package uk.org.tombolo.core.utils;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.execution.spec.DatasetSpecification;
import uk.org.tombolo.execution.spec.SubjectSpecification;
import uk.org.tombolo.execution.spec.SubjectSpecification.SubjectMatchRule;

import java.util.ArrayList;
import java.util.List;

public class SubjectUtils {
	static Logger log = LoggerFactory.getLogger(TimedValueUtils.class);

	public static Subject getSubjectByLabel(String label){
		return HibernateUtil.withSession(session -> {
			return session.createQuery("from Subject where label = :label", Subject.class)
					.setParameter("label", label)
					.uniqueResult();
		});
	}
	
	public static List<Subject> getSubjectByTypeAndLabelPattern(SubjectType subjectType, String labelPattern){
		return HibernateUtil.withSession(session -> {
			return session.createQuery("from Subject where subjectType = :subjectType and lower(label) like :labelPattern", Subject.class)
					.setParameter("subjectType", subjectType)
					.setParameter("labelPattern", labelPattern.toLowerCase())
					.list();
		});
	}

	public static List<Subject> getSubjectBySpecification(DatasetSpecification datasetSpecification) {
		List<Subject> subjects = new ArrayList<>();

		for(SubjectSpecification subjectSpecification : datasetSpecification.getSubjectSpecification()){
			subjects.addAll(getSubjectBySpecification(subjectSpecification));
		}

		return subjects;
	}


	public static List<Subject> getSubjectBySpecification(SubjectSpecification subjectSpecification) {
		return HibernateUtil.withSession(session -> {
			return (List<Subject>) queryFromSubjectSpecification(session, subjectSpecification).list();
		});
	}

	public static List<Subject> getSubjectBySpecifications(List<SubjectSpecification> subjectSpecifications) {
		List<Subject> subjects = new ArrayList<>();
		for (SubjectSpecification subjectSpec : subjectSpecifications) {
			subjects.addAll(getSubjectBySpecification(subjectSpec));
		}
		return subjects;
	}
	
	public static void save(List<Subject> subjects){
		HibernateUtil.withSession(session -> {
			session.beginTransaction();
			int saved = 0;
			for (Subject subject : subjects) {
				Subject savedSubject = getSubjectByLabel(subject.getLabel());

				if (savedSubject == null) {
					session.saveOrUpdate(subject);
				} else {
					// The IDs must be the same so hibernate knows which 'rows' to merge
					subject.setId(savedSubject.getId());
					session.update(session.merge(subject));
				}
				saved++;

				if ( saved % 20 == 0 ) { //20, same as the JDBC batch size
					//flush a batch of inserts and release memory:
					session.flush();
					session.clear();
				}
			}
			session.getTransaction().commit();
		});
	}

	public static Query queryFromSubjectSpecification(Session session, SubjectSpecification subjectSpecification) {
		SubjectType subjectType = SubjectTypeUtils.getSubjectTypeByLabel(subjectSpecification.getSubjectType());

		if (null == subjectSpecification.getMatchRule()) {
			return session.createQuery("from Subject where subjectType = :subjectType", Subject.class)
					.setParameter("subjectType", subjectType);
		}

		Query query;
		if (subjectSpecification.getMatchRule().attribute == SubjectMatchRule.MatchableAttribute.label) {
			query = session.createQuery("from Subject where subjectType = :subjectType and lower(label) like :pattern", Subject.class);
		} else if (subjectSpecification.getMatchRule().attribute == SubjectMatchRule.MatchableAttribute.name) {
			query = session.createQuery("from Subject where subjectType = :subjectType and lower(name) like :pattern", Subject.class);
		} else {
			throw new IllegalArgumentException(String.format("SubjectMatchRule attribute is not a valid type (is %s, can be either name or label)", subjectSpecification.getMatchRule()));
		}

		return query.setParameter("subjectType", subjectType)
				.setParameter("pattern", subjectSpecification.getMatchRule().pattern.toLowerCase());
	}

	public static List<Subject> subjectsContainingSubject(String subjectTypeLabel, Subject subject) {
		return HibernateUtil.withSession(session -> {
			Query query = session.createQuery("from Subject where subjectType = :subjectType and contains(shape, :geom) = true", Subject.class);
			query.setParameter("subjectType", SubjectTypeUtils.getSubjectTypeByLabel(subjectTypeLabel));
			query.setParameter("geom", subject.getShape());
			return (List<Subject>) query.getResultList();
		});
	}

	public static List<Subject> subjectsWithinSubject(String subjectTypeLabel, Subject subject) {
		return HibernateUtil.withSession(session -> {
			Query query = session.createQuery("from Subject where subjectType = :subjectType and within(shape, :geom) = true", Subject.class);
			query.setParameter("subjectType", SubjectTypeUtils.getSubjectTypeByLabel(subjectTypeLabel));
			query.setParameter("geom", subject.getShape());
			return (List<Subject>) query.getResultList();
		});
	}

	public static Subject subjectNearestSubject(String subjectTypeLabel, Subject subject, Double radius){
		return HibernateUtil.withSession(session -> {
			Query query = session.createQuery("from Subject where subjectType = :subjectType and st_dwithin(shape, :geom, :radius) = true order by st_distance(shape, :geom)", Subject.class);
			query.setParameter("subjectType", SubjectTypeUtils.getSubjectTypeByLabel(subjectTypeLabel));
			query.setParameter("geom", subject.getShape());
			query.setParameter("radius", radius);
			query.setMaxResults(1);
			return (Subject) query.uniqueResult();
		});
	}
}
