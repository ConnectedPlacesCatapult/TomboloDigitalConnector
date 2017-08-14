package uk.org.tombolo.field.value;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.TimedValueId;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.field.AbstractField;
import uk.org.tombolo.recipe.AttributeMatcher;
import uk.org.tombolo.field.IncomputableFieldException;
import uk.org.tombolo.field.SingleValueField;

/**
 * LatestValueField.java
 * Returns the latest TimedValue for a particular Attribute on the given subject, plus metadata
 *
 * The metadata is regarding the attribute.
 */
public class LatestValueField extends ValuesByTimeField implements SingleValueField {
    public LatestValueField(String label, AttributeMatcher attribute) {
        super(label, attribute);
    }
}
