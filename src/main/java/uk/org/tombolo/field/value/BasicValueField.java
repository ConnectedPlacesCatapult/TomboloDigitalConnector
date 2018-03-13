package uk.org.tombolo.field.value;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.field.AbstractField;
import uk.org.tombolo.field.IncomputableFieldException;
import uk.org.tombolo.recipe.AttributeMatcher;

/**
 * BasicValueField.java
 *
 * Basic field that contains a label and attribute.
 * The attribute value and metadata for a given subject are specified in the children classes.
 */
public abstract class BasicValueField extends AbstractField {
    private AttributeMatcher attribute;

    public BasicValueField(String label, AttributeMatcher attribute) {
        super(label);
        this.attribute = attribute;
    }

    protected Attribute getAttribute() throws IncomputableFieldException {
        Attribute attr = AttributeUtils.getByProviderAndLabel(attribute.provider, attribute.label);
        if (null == attr) {
            throw new IncomputableFieldException(String.format("No attribute found for provider %s and label %s", attribute.provider, attribute.label));
        }

        return attr;
    }

    protected JSONObject withinMetadata(JSONArray contents) {
        JSONObject obj = new JSONObject();
        obj.put(label, contents);
        return obj;
    }

    protected JSONObject withinMetadata(JSONObject contents) {
        JSONObject obj = new JSONObject();
        obj.put(label, contents);
        return obj;
    }
}
