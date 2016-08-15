package uk.org.tombolo.core.utils;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.execution.spec.DatasetSpecification;
import uk.org.tombolo.execution.spec.SubjectSpecification;
import uk.org.tombolo.execution.spec.SubjectSpecification.SubjectMatcher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

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

	public static List<Subject> subjectsContainingSubject(String subjectTypeLabel, Subject subject) {
		return HibernateUtil.withSession(session -> {
			Query query = session.createQuery("select s from Subject s where subjectType = :subjectType and contains(s.shape, :geom) = true", Subject.class);
			query.setParameter("subjectType", SubjectTypeUtils.getSubjectTypeByLabel(subjectTypeLabel));
			query.setParameter("geom", subject.getShape());
			return (List<Subject>) query.getResultList();
		});
	}
}
