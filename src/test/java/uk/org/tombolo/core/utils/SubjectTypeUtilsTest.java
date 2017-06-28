package uk.org.tombolo.core.utils;

import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.SubjectType;

import static org.junit.Assert.assertEquals;

public class SubjectTypeUtilsTest extends AbstractTest {
	
	@Test
	public void testGetSubjectTypeByLabel(){
		SubjectType lsoa = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(TestFactory.DEFAULT_PROVIDER.getLabel(), "unknown");
		assertEquals("unknown", lsoa.getLabel());
		assertEquals("Unknown Subject Type", lsoa.getName());
	}

	@Test
	public void testSaveSubjectType(){
		SubjectType subjectType1 = new SubjectType(TestFactory.DEFAULT_PROVIDER, "test", "Test name 1");
		SubjectType subjectType2 = new SubjectType(TestFactory.DEFAULT_PROVIDER, "test", "Test name 2");
		SubjectType testSubjectType = null;

		SubjectTypeUtils.save(subjectType1);
		testSubjectType = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(TestFactory.DEFAULT_PROVIDER.getLabel(), "test");
		assertEquals(subjectType1.getName(), testSubjectType.getName());

		// Test updated subject type
		SubjectTypeUtils.save(subjectType2);
		testSubjectType = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(TestFactory.DEFAULT_PROVIDER.getLabel(), "test");
		assertEquals(subjectType2.getName(), testSubjectType.getName());

	}
}
