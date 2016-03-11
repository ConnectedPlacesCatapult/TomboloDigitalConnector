package uk.org.tombolo.core.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import uk.org.tombolo.core.Attribute;

public class AttributeUtils {

	public static Attribute getTestAttribute(){
		Session session = HibernateUtil.getSessionFactory().openSession();
		Criteria criteria = session.createCriteria(Attribute.class);
		Map<String,Object> restrictions = new HashMap<String,Object>();
		restrictions.put("provider", ProviderUtils.getTestProvider());
		restrictions.put("label", "testAttribute");
		return (Attribute)criteria.add(Restrictions.allEq(restrictions)).uniqueResult();
	}
	
	public static void save(List<Attribute> attributes){
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		for (Attribute attribute : attributes){
			// FIXME: This might be inefficient if we are updating the attribute over and over again without actually changing it			
			Criteria criteria = session.createCriteria(Attribute.class);
			Map<String,Object> restrictions = new HashMap<String,Object>();
			restrictions.put("provider", attribute.getProvider());
			restrictions.put("label", attribute.getLabel());
			Attribute savedAttribute = (Attribute)criteria.add(Restrictions.allEq(restrictions)).uniqueResult();
			if (savedAttribute == null){
				Integer id = (Integer)session.save(attribute);
				attribute.setId(id);
			}else{
				attribute.setId(savedAttribute.getId());
			}
		}
		session.getTransaction().commit();
	}

}
