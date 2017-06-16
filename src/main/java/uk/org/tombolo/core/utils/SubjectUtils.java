package uk.org.tombolo.core.utils;

import com.vividsolutions.jts.geom.Geometry;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.execution.spec.DatasetSpecification;
import uk.org.tombolo.execution.spec.SubjectSpecification;
import uk.org.tombolo.execution.spec.SubjectSpecification.SubjectAttributeMatchRule;

import javax.persistence.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SubjectUtils {
	static Logger log = LoggerFactory.getLogger(TimedValueUtils.class);

	@Deprecated
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

	public static Subject getSubjectByTypeAndLabel(SubjectType subjectType, String label){
		return HibernateUtil.withSession(session -> {
			return session.createQuery("from Subject where subjectType = :subjectType and label = :label", Subject.class)
					.setParameter("subjectType", subjectType)
					.setParameter("label", label)
					.uniqueResult();
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
				Subject savedSubject = getSubjectByTypeAndLabel(subject.getSubjectType(), subject.getLabel());

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

	private static Query queryFromSubjectSpecification(Session session, SubjectSpecification subjectSpecification) {
		SubjectType subjectType = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(subjectSpecification.getProvider(), subjectSpecification.getSubjectType());

		String hqlQuery = "from Subject s where s.subjectType = :subjectType";

		// Add Attribute Match Rule if exists
		if (null != subjectSpecification.getMatchRule()){
			if (subjectSpecification.getMatchRule().attribute == SubjectAttributeMatchRule.MatchableAttribute.label) {
				hqlQuery += " and lower(label) like :pattern";
			} else if (subjectSpecification.getMatchRule().attribute == SubjectAttributeMatchRule.MatchableAttribute.name) {
				hqlQuery += " and lower(name) like :pattern";
			} else {
				throw new IllegalArgumentException(String.format(
						"SubjectAttributeMatchRule attribute is not a valid type (is %s, can be either name or label)",
						subjectSpecification.getMatchRule().attribute.name()));
			}
		}

		// Add Geo Match Rule if exists
		if (null != subjectSpecification.getGeoMatchRule()){
			if (subjectSpecification.getGeoMatchRule().geoRelation == SubjectSpecification.SubjectGeoMatchRule.GeoRelation.within){
				hqlQuery += " and within(shape, :geom) = true";
			}else{
				throw new IllegalArgumentException(String.format(
						"SubjectGeoMatchRule attribute is not a valid type (is %s, can only be within)",
						subjectSpecification.getGeoMatchRule().geoRelation.name()));
			}
		}

		// Create the basic query with obligatory paramaters
		Query query = session.createQuery(hqlQuery, Subject.class);

		for (Parameter parameter : query.getParameters()) {
			if (Objects.equals(parameter.getName(), "subjectType")) {
				query.setParameter("subjectType", subjectType);
			} else if (Objects.equals(parameter.getName(), "pattern")) {
				query.setParameter("pattern", subjectSpecification.getMatchRule().pattern.toLowerCase());
			} else if (Objects.equals(parameter.getName(), "geom")) {
				List<Subject> parents = getSubjectBySpecifications(subjectSpecification.getGeoMatchRule().subjectSpecifications);

				Geometry union = null;
				for (Subject parent : parents){
					if (union == null)
						union = parent.getShape();
					else
						union = union.union(parent.getShape());
				}
				union.setSRID(Subject.SRID);
				query.setParameter("geom", union);
			}
		}

		return query;
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
