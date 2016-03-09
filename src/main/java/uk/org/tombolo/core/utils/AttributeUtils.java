package uk.org.tombolo.core.utils;

import java.util.HashMap;
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
}
