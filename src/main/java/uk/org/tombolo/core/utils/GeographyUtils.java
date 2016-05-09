package uk.org.tombolo.core.utils;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import uk.org.tombolo.core.Geography;
import uk.org.tombolo.core.GeographyType;
import uk.org.tombolo.execution.spec.DatasetSpecification;
import uk.org.tombolo.execution.spec.GeographySpecification;
import uk.org.tombolo.execution.spec.GeographySpecification.GeographyMatcher;

public class GeographyUtils {

	static Session session = HibernateUtil.getSessionFactory().openSession();

	public static Geography getGeographyByLabel(String label){
		Criteria criteria = session.createCriteria(Geography.class);
		return (Geography)criteria.add(Restrictions.eq("label", label)).uniqueResult();		
	}
	
	public static List<Geography> getGeographyByTypeAndLabelPattern(GeographyType geographyType, String labelPattern){
		Criteria criteria = session.createCriteria(Geography.class);
		criteria = criteria.add(Restrictions.eq("geographyType", geographyType));
		if (labelPattern != null)
			criteria = criteria.add(Restrictions.like("label", labelPattern));
		
		// FIXME: This should be paginated
		return (List<Geography>) criteria.list();		
	}

	public static List<Geography> getGeographyBySpecification(DatasetSpecification datasetSpecification) {
		List<Geography> geographies = new ArrayList<>();

		for(GeographySpecification geographySpecification : datasetSpecification.getGeographySpecification()){
			geographies.addAll(getGeographyBySpecification(geographySpecification));
		}

		return geographies;
	}


	public static List<Geography> getGeographyBySpecification(GeographySpecification geographySpecification) {
		return criteriaFromGeographySpecification(geographySpecification).list();
	}
	
	public static void save(List<Geography> geographyObjects){
		session.beginTransaction();
		for(Geography geography : geographyObjects){
			Criteria criteria = session.createCriteria(Geography.class);
			Geography savedGeography = (Geography)criteria.add(Restrictions.eq("label", geography.getLabel())).uniqueResult();
			if (savedGeography == null){
				Integer id = (Integer)session.save(geography);
				geography.setId(id);
			}else{
				// FIXME: Find a way to update an existing ... if needed
				//geography.setId(savedGeography.getId());
				//session.saveOrUpdate(geography);
			}
		}
		session.getTransaction().commit();
	}
	
	public static Geography getTestGeography(){
		Criteria criteria = session.createCriteria(Geography.class);
		return (Geography)criteria
				.add(Restrictions.eq("label", "E01000001"))
				.uniqueResult();
	}

	public static Criteria criteriaFromGeographySpecification(GeographySpecification geographySpecification) {
		GeographyType geographyType = GeographyTypeUtils.getGeographyTypeByLabel(geographySpecification.getGeographyType());
		Criteria criteria = session.createCriteria(Geography.class);
		criteria.add(Restrictions.eq("geographyType", geographyType));

		for (GeographyMatcher matcher : geographySpecification.getMatchers())
			criteria.add(Restrictions.like(matcher.attribute, matcher.pattern));

		return criteria;
	}
}
