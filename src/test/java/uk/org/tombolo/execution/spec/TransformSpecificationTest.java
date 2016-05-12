package uk.org.tombolo.execution.spec;

import org.junit.Test;
import uk.org.tombolo.core.Attribute;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class TransformSpecificationTest {

    @Test
    public void testGetTransformClass() throws Exception {
        TransformSpecification tSpec = new TransformSpecification(new ArrayList<>(), new Attribute(), "className");
        assertEquals("className", tSpec.getTransformClass());
    }

    @Test
    public void testGetOutputAttribute() throws Exception {
        Attribute attribute = new Attribute();
        TransformSpecification tSpec = new TransformSpecification(new ArrayList<>(), attribute, "className");
        assertSame(attribute, tSpec.getOutputAttribute());
    }
}