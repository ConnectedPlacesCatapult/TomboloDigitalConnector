package uk.org.tombolo.execution.spec;

import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.Attribute;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class TransformSpecificationTest extends AbstractTest {

    @Test
    public void testGettransformerClass() throws Exception {
        TransformSpecification tSpec = new TransformSpecification(new ArrayList<>(), new Attribute(), "className");
        assertEquals("className", tSpec.gettransformerClass());
    }

    @Test
    public void testGetOutputAttribute() throws Exception {
        Attribute attribute = new Attribute();
        TransformSpecification tSpec = new TransformSpecification(new ArrayList<>(), attribute, "className");
        assertSame(attribute, tSpec.getOutputAttribute());
    }
}