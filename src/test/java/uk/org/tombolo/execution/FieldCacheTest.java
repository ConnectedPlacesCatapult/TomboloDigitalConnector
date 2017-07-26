package uk.org.tombolo.execution;

import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.FieldSpecificationBuilder;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.field.Field;

import static org.junit.Assert.*;

public class FieldCacheTest extends AbstractTest {
    FieldCache fieldCache = new FieldCache();

    @Test
    public void getChachedValue() throws Exception {
        Field field = FieldSpecificationBuilder.fixedAnnotationField("mylabel", "myvalue").build().toField();
        Subject subject = TestFactory.makeNamedSubject("E01000001");

        // Nothing has been put into cache
        assertNull(fieldCache.getChachedValue(field, subject));

        // Lets cache
        fieldCache.putCachedValue(field, subject, "testvalue");
        assertEquals("testvalue", fieldCache.getChachedValue(field, subject));

        // Lets recache
        fieldCache.putCachedValue(field, subject, "testvalue2");
        assertEquals("testvalue2", fieldCache.getChachedValue(field, subject));
    }

}