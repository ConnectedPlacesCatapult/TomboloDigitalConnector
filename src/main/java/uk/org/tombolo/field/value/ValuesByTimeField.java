package uk.org.tombolo.field.value;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.TimedValueId;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.field.SingleValueField;
import uk.org.tombolo.recipe.AttributeMatcher;
import uk.org.tombolo.field.AbstractField;
import uk.org.tombolo.field.Field;
import uk.org.tombolo.field.IncomputableFieldException;

import java.util.stream.Collectors;

/**
 * ValuesByTimeField.java
 * Returns all TimedValues on an Attribute for a given Subject, plus metadata.
 *
 * The metadata is regarding the attribute.
 */
public class ValuesByTimeField extends AbstractField implements SingleValueField {

    public ValuesByTimeField(String label) {
        super(label);
    }

    public ValuesByTimeField(String label, AttributeMatcher attribute) {
        super(label, attribute);
    }
}
