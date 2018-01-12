package uk.org.tombolo.core.utils;

import com.vividsolutions.jts.geom.Geometry;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.recipe.DatasetRecipe;
import uk.org.tombolo.recipe.SubjectRecipe;
import uk.org.tombolo.recipe.SubjectRecipe.SubjectAttributeMatchRule;

import javax.persistence.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SubjectUtils {
	static Logger log = LoggerFactory.getLogger(TimedValueUtils.class);

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

	public static List<Subject> getSubjectByTypeAndNamePattern(SubjectType subjectType, String namePattern){
		return HibernateUtil.withSession(session -> {
			return session.createQuery("from Subject where subjectType = :subjectType and lower(name) like :namePattern", Subject.class)
					.setParameter("subjectType", subjectType)
					.setParameter("namePattern", namePattern.toLowerCase())
					.list();
		});
	}

	public static List<Subject> getSubjectByTypeAndName(SubjectType subjectType, String name){
		return HibernateUtil.withSession(session -> {
			return session.createQuery("from Subject where subjectType = :subjectType and name = :name", Subject.class)
					.setParameter("subjectType", subjectType)
					.setParameter("name", name)
					.list();
		});
	}

	public static List<Subject> getSubjectByTypeAndNameIsNull(SubjectType subjectType, boolean includeNull){
		return HibernateUtil.withSession(session -> {
			Query query;
			if (includeNull) query = session.createQuery("from Subject where subjectType = :subjectType and name is null", Subject.class);
			else query = session.createQuery("from Subject where subjectType = :subjectType and name is not null", Subject.class);
			return query.setParameter("subjectType", subjectType).list();
		});
	}

	public static List<Subject> getSubjectBySpecification(DatasetRecipe datasetRecipe) {
		List<Subject> subjects = new ArrayList<>();

		for(SubjectRecipe subjectRecipe : datasetRecipe.getSubjects()){
			subjects.addAll(getSubjectBySpecification(subjectRecipe));
		}

		return subjects;
	}


	public static List<Subject> getSubjectBySpecification(SubjectRecipe subjectRecipe) {
		return HibernateUtil.withSession(session -> {
			return (List<Subject>) queryFromSubjectSpecification(session, subjectRecipe).list();
		});
	}

	public static List<Subject> getSubjectBySpecifications(List<SubjectRecipe> subjectRecipes) {
		List<Subject> subjects = new ArrayList<>();
		for (SubjectRecipe subjectSpec : subjectRecipes) {
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

	private static Query queryFromSubjectSpecification(Session session, SubjectRecipe subjectRecipe) {
		SubjectType subjectType = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(subjectRecipe.getProvider(), subjectRecipe.getSubjectType());

		String hqlQuery = "from Subject s where s.subjectType = :subjectType";

		// Add Attribute Match Rule if exists
		if (null != subjectRecipe.getMatchRule()){
			if (subjectRecipe.getMatchRule().attribute == SubjectAttributeMatchRule.MatchableAttribute.label) {
				hqlQuery += " and lower(label) like :pattern";
			} else if (subjectRecipe.getMatchRule().attribute == SubjectAttributeMatchRule.MatchableAttribute.name) {
				hqlQuery += " and lower(name) like :pattern";
			} else {
				throw new IllegalArgumentException(String.format(
						"SubjectAttributeMatchRule attribute is not a valid type (is %s, can be either name or label)",
						subjectRecipe.getMatchRule().attribute.name()));
			}
		}

		// Add Geo Match Rule if exists
		if (null != subjectRecipe.getGeoMatchRule()){
			if (subjectRecipe.getGeoMatchRule().geoRelation == SubjectRecipe.SubjectGeoMatchRule.GeoRelation.within){
				hqlQuery += " and within(shape, :geom) = true";
			}else{
				throw new IllegalArgumentException(String.format(
						"SubjectGeoMatchRule attribute is not a valid type (is %s, can only be within)",
						subjectRecipe.getGeoMatchRule().geoRelation.name()));
			}
		}

		// Create the basic query with obligatory paramaters
		Query query = session.createQuery(hqlQuery, Subject.class);

		for (Parameter parameter : query.getParameters()) {
			if (Objects.equals(parameter.getName(), "subjectType")) {
				query.setParameter("subjectType", subjectType);
			} else if (Objects.equals(parameter.getName(), "pattern")) {
				query.setParameter("pattern", subjectRecipe.getMatchRule().pattern.toLowerCase());
			} else if (Objects.equals(parameter.getName(), "geom")) {
				List<Subject> parents = getSubjectBySpecifications(subjectRecipe.getGeoMatchRule().subjects);

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

	public static List<Subject> subjectsContainingSubject(SubjectType subjectType, Subject subject) {
		return HibernateUtil.withSession(session -> {
			Query query = session.createQuery("from Subject where subjectType = :subjectType and contains(shape, :geom) = true", Subject.class);
			query.setParameter("subjectType", subjectType);
			query.setParameter("geom", subject.getShape());
			query.setCacheable(true);
			return (List<Subject>) query.getResultList();
		});
	}

	public static List<Subject> subjectsWithinSubject(SubjectType subjectType, Subject subject) {
		return HibernateUtil.withSession(session -> {
			Query query = session.createQuery("from Subject where subjectType = :subjectType and within(shape, :geom) = true", Subject.class);
			query.setParameter("subjectType", subjectType);
			query.setParameter("geom", subject.getShape());
			query.setCacheable(true);
			return (List<Subject>) query.getResultList();
		});
	}

	public static Subject subjectNearestSubject(SubjectType subjectType, Subject subject, Double radius){
		return HibernateUtil.withSession(session -> {
			Query query = session.createQuery("from Subject where subjectType = :subjectType and st_dwithin(shape, :geom, :radius) = true order by st_distance(shape, :geom)", Subject.class);
			query.setParameter("subjectType", subjectType);
			query.setParameter("geom", subject.getShape());
			query.setParameter("radius", radius);
			query.setCacheable(true);
			query.setMaxResults(1);
			return (Subject) query.uniqueResult();
		});
	}
}
