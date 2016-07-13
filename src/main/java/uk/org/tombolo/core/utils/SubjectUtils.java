package uk.org.tombolo.core.utils;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.execution.spec.DatasetSpecification;
import uk.org.tombolo.execution.spec.SubjectSpecification;
import uk.org.tombolo.execution.spec.SubjectSpecification.SubjectMatcher;

import java.util.ArrayList;
import java.util.List;

public class SubjectUtils {
	static Logger log = LoggerFactory.getLogger(TimedValueUtils.class);

	public static Subject getSubjectByLabel(String label){
		return HibernateUtil.withSession(session -> {
			Criteria criteria = session.createCriteria(Subject.class);
			return (Subject) criteria.add(Restrictions.eq("label", label)).uniqueResult();
		});
	}
	
	public static List<Subject> getSubjectByTypeAndLabelPattern(SubjectType subjectType, String labelPattern){
		return HibernateUtil.withSession(session -> {
			Criteria criteria = session.createCriteria(Subject.class);
			criteria = criteria.add(Restrictions.eq("subjectType", subjectType));
			if (labelPattern != null)
				criteria = criteria.add(Restrictions.like("label", labelPattern));

			// FIXME: This should be paginated
			return (List<Subject>) criteria.list();
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
			return (List<Subject>) criteriaFromSubjectSpecification(session, subjectSpecification).list();
		});
	}
	
	public static void save(List<Subject> subjects){
		HibernateUtil.withSession(session -> {
			session.beginTransaction();
			int saved = 0;
			for (Subject subject : subjects) {
				Criteria criteria = session.createCriteria(Subject.class);
				Subject savedSubject = (Subject) criteria.add(Restrictions.eq("label", subject.getLabel())).uniqueResult();

				if (savedSubject == null) {
					session.saveOrUpdate(subject);
					saved++;
				} else {
					// This is happening because the TFL stations contain a duplicate ID, amongst other reasons
					log.warn("Could not save subject {} {}. Original message: {}",
							subject.getLabel(),
							subject.getName());
				}

				if ( saved % 20 == 0 ) { //20, same as the JDBC batch size
					//flush a batch of inserts and release memory:
					session.flush();
					session.clear();
				}
			}
			session.getTransaction().commit();
		});
	}
	
	public static Subject getTestSubject(){
		return HibernateUtil.withSession(session -> {
			Criteria criteria = session.createCriteria(Subject.class);
			return (Subject) criteria
					.add(Restrictions.eq("label", "E01000001"))
					.uniqueResult();
		});
	}

	public static Criteria criteriaFromSubjectSpecification(Session session, SubjectSpecification subjectSpecification) {
		SubjectType subjectType = SubjectTypeUtils.getSubjectTypeByLabel(subjectSpecification.getSubjectType());
		Criteria criteria = session.createCriteria(Subject.class);
		criteria.add(Restrictions.eq("subjectType", subjectType));

		for (SubjectMatcher matcher : subjectSpecification.getMatchers())
			criteria.add(Restrictions.like(matcher.attribute, matcher.pattern));

		return criteria;
	}
}
