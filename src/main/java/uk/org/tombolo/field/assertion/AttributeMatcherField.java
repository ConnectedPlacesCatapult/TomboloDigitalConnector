package uk.org.tombolo.field.assertion;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.FixedValue;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.FixedValueUtils;
import uk.org.tombolo.field.*;
import uk.org.tombolo.recipe.AttributeMatcher;
import uk.org.tombolo.recipe.FieldRecipe;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Returns the value of the 'field' if the subject has an attribute matching one of the 'attributes'"
 */
public class AttributeMatcherField extends AbstractField implements SingleValueField, ParentField{

    private List<AttributeMatcher> attributes;
    private FieldRecipe field;

    private Map<Attribute, List<String>> attributeValueMatches;
    private SingleValueField singleValueField;

    public AttributeMatcherField(String label, List<AttributeMatcher> attributes, FieldRecipe field) {
        super(label);
        this.attributes = attributes;
        this.field = field;
    }

    protected Map<Attribute, List<String>> getAttributeValueMatches(AttributeMatcher attributeMatcher) {
        Map<Attribute, List<String>> matches = new HashMap<>();
        matches.put(AttributeUtils.getByProviderAndLabel(
                attributeMatcher.provider,
                attributeMatcher.label),
                attributeMatcher.values);

        return matches;
    }

    public void initialize() {
        attributeValueMatches = new HashMap<>();
        attributes.stream().forEach(attributeMatcher -> attributeValueMatches.putAll(
                getAttributeValueMatches(attributeMatcher)));

        try {
            this.singleValueField = (SingleValueField) field.toField();
            singleValueField.setFieldCache(fieldCache);
        } catch (Exception e) {
            throw new Error("Field not valid", e);
        }
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        JSONObject obj = new JSONObject();
        obj.put(this.label, valueForSubject(subject, timeStamp));
        return obj;
    }

    @Override
    public String valueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        String cachedValue = getCachedValue(subject);
        if (cachedValue != null)
            return cachedValue;
        if (attributeValueMatches == null)
            initialize();
        for (Attribute cachedAttribute : attributeValueMatches.keySet()) {
            FixedValue fixedValue = FixedValueUtils.getBySubjectAndAttribute(subject, cachedAttribute);
            if (fixedValue != null) {
                List<String> values = attributeValueMatches.get(cachedAttribute);
                if (values == null || values.isEmpty() || values.contains("*")) {
                    return getFieldValue(subject, timeStamp);
                }
                for (String value : values) {
                    if (fixedValue.getValue().equals(value)) {
                        return getFieldValue(subject, timeStamp);
                    }
                }
            }
        }
        setCachedValue(subject, "0");
        return "0";
    }

    private String getFieldValue(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        String fieldValue = singleValueField.valueForSubject(subject, timeStamp);
        setCachedValue(subject, fieldValue);
        return fieldValue;
    }

    @Override
    public List<Field> getChildFields() {
        if (singleValueField == null)
            initialize();
        return Collections.singletonList(singleValueField);
    }
}
