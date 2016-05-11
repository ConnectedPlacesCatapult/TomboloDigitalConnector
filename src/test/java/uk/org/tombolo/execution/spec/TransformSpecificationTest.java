package uk.org.tombolo.execution.spec;

import org.junit.Test;
import uk.org.tombolo.core.Attribute;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TransformSpecificationTest {

    @Test
    public void testGetTransformClass() throws Exception {
        TransformSpecification tSpec = new TransformSpecification(new ArrayList<Attribute>(), new Attribute(), "className");
        assertEquals("className", tSpec.getTransformClass());
    }

    @Test
    public void testGetInputAttributes() throws Exception {
        List<Attribute> attributeList = new ArrayList<Attribute>();
        TransformSpecification tSpec = new TransformSpecification(attributeList, new Attribute(), "className");
        assertSame(attributeList, tSpec.getInputAttributes());
    }

    @Test
    public void testGetOutputAttribute() throws Exception {
        Attribute attribute = new Attribute();
        TransformSpecification tSpec = new TransformSpecification(new ArrayList<Attribute>(), attribute, "className");
        assertSame(attribute, tSpec.getOutputAttribute());
    }
}