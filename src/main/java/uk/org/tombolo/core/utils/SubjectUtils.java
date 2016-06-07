package uk.org.tombolo.core.utils;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.execution.spec.DatasetSpecification;
import uk.org.tombolo.execution.spec.GeographySpecification;
import uk.org.tombolo.execution.spec.GeographySpecification.GeographyMatcher;

public class SubjectUtils {

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
		List<Subject> geographies = new ArrayList<>();

		for(GeographySpecification geographySpecification : datasetSpecification.getGeographySpecification()){
			geographies.addAll(getSubjectBySpecification(geographySpecification));
		}

		return geographies;
	}


	public static List<Subject> getSubjectBySpecification(GeographySpecification geographySpecification) {
		return HibernateUtil.withSession(session -> {
			return (List<Subject>) criteriaFromGeographySpecification(session, geographySpecification).list();
		});
	}
	
	public static void save(List<Subject> subjectObjects){
		HibernateUtil.withSession(session -> {
			session.beginTransaction();
			for (Subject subject : subjectObjects) {
				Criteria criteria = session.createCriteria(Subject.class);
				Subject savedSubject = (Subject) criteria.add(Restrictions.eq("label", subject.getLabel())).uniqueResult();
				if (savedSubject == null) {
					Integer id = (Integer) session.save(subject);
					subject.setId(id);
				} else {
					// FIXME: Find a way to update an existing ... if needed
					//subject.setId(savedSubject.getId());
					//session.saveOrUpdate(subject);
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

	public static Criteria criteriaFromGeographySpecification(Session session, GeographySpecification geographySpecification) {
		SubjectType subjectType = SubjectTypeUtils.getSubjectTypeByLabel(geographySpecification.getGeographyType());
		Criteria criteria = session.createCriteria(Subject.class);
		criteria.add(Restrictions.eq("subjectType", subjectType));

		for (GeographyMatcher matcher : geographySpecification.getMatchers())
			criteria.add(Restrictions.like(matcher.attribute, matcher.pattern));

		return criteria;
	}
}
