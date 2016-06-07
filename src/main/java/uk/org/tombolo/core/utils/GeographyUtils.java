package uk.org.tombolo.core.utils;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.GeographyType;
import uk.org.tombolo.execution.spec.DatasetSpecification;
import uk.org.tombolo.execution.spec.GeographySpecification;
import uk.org.tombolo.execution.spec.GeographySpecification.GeographyMatcher;

public class GeographyUtils {

	public static Subject getGeographyByLabel(String label){
		return HibernateUtil.withSession(session -> {
			Criteria criteria = session.createCriteria(Subject.class);
			return (Subject) criteria.add(Restrictions.eq("label", label)).uniqueResult();
		});
	}
	
	public static List<Subject> getGeographyByTypeAndLabelPattern(GeographyType geographyType, String labelPattern){
		return HibernateUtil.withSession(session -> {
			Criteria criteria = session.createCriteria(Subject.class);
			criteria = criteria.add(Restrictions.eq("geographyType", geographyType));
			if (labelPattern != null)
				criteria = criteria.add(Restrictions.like("label", labelPattern));

			// FIXME: This should be paginated
			return (List<Subject>) criteria.list();
		});
	}

	public static List<Subject> getGeographyBySpecification(DatasetSpecification datasetSpecification) {
		List<Subject> geographies = new ArrayList<>();

		for(GeographySpecification geographySpecification : datasetSpecification.getGeographySpecification()){
			geographies.addAll(getGeographyBySpecification(geographySpecification));
		}

		return geographies;
	}


	public static List<Subject> getGeographyBySpecification(GeographySpecification geographySpecification) {
		return HibernateUtil.withSession(session -> {
			return (List<Subject>) criteriaFromGeographySpecification(session, geographySpecification).list();
		});
	}
	
	public static void save(List<Subject> geographyObjects){
		HibernateUtil.withSession(session -> {
			session.beginTransaction();
			for (Subject geography : geographyObjects) {
				Criteria criteria = session.createCriteria(Subject.class);
				Subject savedGeography = (Subject) criteria.add(Restrictions.eq("label", geography.getLabel())).uniqueResult();
				if (savedGeography == null) {
					Integer id = (Integer) session.save(geography);
					geography.setId(id);
				} else {
					// FIXME: Find a way to update an existing ... if needed
					//geography.setId(savedGeography.getId());
					//session.saveOrUpdate(geography);
				}
			}
			session.getTransaction().commit();
		});
	}
	
	public static Subject getTestGeography(){
		return HibernateUtil.withSession(session -> {
			Criteria criteria = session.createCriteria(Subject.class);
			return (Subject) criteria
					.add(Restrictions.eq("label", "E01000001"))
					.uniqueResult();
		});
	}

	public static Criteria criteriaFromGeographySpecification(Session session, GeographySpecification geographySpecification) {
		GeographyType geographyType = GeographyTypeUtils.getGeographyTypeByLabel(geographySpecification.getGeographyType());
		Criteria criteria = session.createCriteria(Subject.class);
		criteria.add(Restrictions.eq("geographyType", geographyType));

		for (GeographyMatcher matcher : geographySpecification.getMatchers())
			criteria.add(Restrictions.like(matcher.attribute, matcher.pattern));

		return criteria;
	}
}
