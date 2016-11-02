package uk.org.tombolo.core.utils;

import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.FixedValue;
import uk.org.tombolo.core.Subject;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class FixedValueUtilsTest extends AbstractTest {

    @Test
    public void testGetBySubjectAndAttribute() throws Exception {
        Subject subject = TestFactory.makeNamedSubject("E01000001");
        Attribute attribute = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attrLabel");
        FixedValue fixedValue = TestFactory.makeFixedValue(subject.getLabel(), attribute, "thirteen");
        assertEquals(
                fixedValue,
                FixedValueUtils.getBySubjectAndAttribute(subject, attribute));
    }

    @Test
    public void testSaveMultiple() throws Exception {
        Subject subject = TestFactory.makeNamedSubject("E01000001");
        Attribute attribute = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attrLabel");
        Attribute attribute2 = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attrLabel2");
        FixedValue fixedValue = new FixedValue(subject, attribute, "one");
        FixedValue fixedValue2 = new FixedValue(subject, attribute2, "two");
        FixedValueUtils.save(Arrays.asList(fixedValue, fixedValue2));
        assertEquals(
                fixedValue,
                FixedValueUtils.getBySubjectAndAttribute(subject, attribute));
        assertEquals(
                fixedValue2,
                FixedValueUtils.getBySubjectAndAttribute(subject, attribute2));
    }
}