package uk.org.tombolo.core.utils;

import org.hibernate.NonUniqueObjectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.FixedValue;
import uk.org.tombolo.core.Subject;

import java.util.Collections;
import java.util.List;

public class FixedValueUtils {
    static Logger log = LoggerFactory.getLogger(FixedValueUtils.class);

    public static FixedValue getBySubjectAndAttribute(Subject subject, Attribute attribute){
        return HibernateUtil.withSession((session) -> {
            return session.createQuery("from FixedValue where id.subject = :subject and id.attribute = :attribute", FixedValue.class)
                    .setParameter("subject", subject)
                    .setParameter("attribute", attribute)
                    .uniqueResult();
        });
    }

    public static void save(FixedValue fixedValue){
        save(Collections.singletonList(fixedValue));
    }

    public static int save(List<FixedValue> fixedValues){
        return HibernateUtil.withSession((session) -> {
            int saved = 0;
            session.beginTransaction();
            for (FixedValue fixedValue : fixedValues){
                try{
                    session.saveOrUpdate(fixedValue);
                    saved++;
                }catch(NonUniqueObjectException e){
                    // This is happening because the TFL stations contain a duplicate ID
                    log.warn("Could not save fixed value for subject {}, attribute {}: {}",
                            fixedValue.getId().getSubject().getLabel(),
                            fixedValue.getId().getAttribute().getLabel(),
                            e.getMessage());
                }
                if ( saved % 20 == 0 ) { //20, same as the JDBC batch size
                    //flush a batch of inserts and release memory:
                    session.flush();
                    session.clear();
                }
            }
            session.getTransaction().commit();
            return saved;
        });
    }

    /*
	Save and update requries to check in the database whether the entry exists or not,
	if exists it updates else adds, but that increase overhead and compute time.
	Using this method will only keep the old value and discard the new one, in case of 
	duplicate records.
	FIXME: Need to find a better way to address it
	*/
    public static int saveWithoutUpdate(List<FixedValue> fixedValues){
        return HibernateUtil.withSession((session) -> {
            int saved = 0;
            session.beginTransaction();
            for (FixedValue fixedValue : fixedValues){
                try{
                    session.save(fixedValue);
                    saved++;
                }catch(NonUniqueObjectException e){
                    log.warn("Could not save fixed value for subject {}, attribute {}: {}",
                            fixedValue.getId().getSubject().getLabel(),
                            fixedValue.getId().getAttribute().getLabel(),
                            e.getMessage());
                }
                if ( saved % 2000 == 0 ) { 
                    // FIXME:
					// Flushing at small intervals increase overhead for the system to clear the session.
					// The default behaviour of hibernate is to auto flush when it thinks is necessary thus it may be required to 
					// flush the session manually but this requires testing, and can be cosidered as fixme
                    session.flush();
                    session.clear();
                }
            }
            session.getTransaction().commit();
            return saved;
        });
    }
}
