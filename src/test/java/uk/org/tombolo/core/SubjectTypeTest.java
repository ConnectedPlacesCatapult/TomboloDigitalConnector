package uk.org.tombolo.core;

import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.utils.HibernateUtil;
import uk.org.tombolo.core.utils.SubjectTypeUtils;

import static org.junit.Assert.assertEquals;

public class SubjectTypeTest extends AbstractTest {

	@Test
	public void testFixtures(){
		HibernateUtil.withSession(session -> {
			SubjectType unknown = (SubjectType) SubjectTypeUtils.getSubjectTypeByProviderAndLabel("default_provider_label", "unknown");
			assertEquals("unknown", unknown.getLabel());
			assertEquals("Unknown Subject Type", unknown.getName());
		});
	}
	
}
