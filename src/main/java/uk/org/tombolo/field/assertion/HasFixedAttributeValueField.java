package uk.org.tombolo.field.assertion;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.FixedValue;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.FixedValueUtils;
import uk.org.tombolo.field.AbstractField;
import uk.org.tombolo.field.IncomputableFieldException;
import uk.org.tombolo.field.SingleValueField;
import uk.org.tombolo.importer.osm.BuiltInImporters;
import uk.org.tombolo.recipe.AttributeMatcher;
import uk.org.tombolo.recipe.FieldRecipe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Returns 1 if subject has attribute with value
 */
public class HasFixedAttributeValueField extends AbstractField {

    private List<AttributeMatcher> attributes;
    private FieldRecipe field;

    private Map<Attribute, List<String>> cachedValueAttributes;
    private SingleValueField singleValueField;

    public HasFixedAttributeValueField(String label, List<AttributeMatcher> attributes, FieldRecipe field) {
        super(label);
        this.attributes = attributes;
        this.field = field;
    }

    public void initialize() {
        cachedValueAttributes = new HashMap<>();
        for (AttributeMatcher attributeMatcher: attributes) {
            if (!checkOSMBuiltIn(attributeMatcher)) {
                attributes.stream().forEach(attribute -> cachedValueAttributes
                        .put(AttributeUtils.getByProviderAndLabel(attribute.provider, attribute.label),
                                attribute.values));
            }
        }

        try {
            this.singleValueField = (SingleValueField) field.toField();
            singleValueField.setFieldCache(fieldCache);
        } catch (Exception e) {
            throw new Error("Field not valid", e);
        }
    }

    @Override
    public String valueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        String cachedValue = getCachedValue(subject);
        if (cachedValue != null)
            return cachedValue;
        if (cachedValueAttributes == null)
            initialize();
        for (Attribute cachedAttribute : cachedValueAttributes.keySet()) {
            FixedValue fixedValue = FixedValueUtils.getBySubjectAndAttribute(subject, cachedAttribute);
            if (fixedValue != null) {
                List<String> values = cachedValueAttributes.get(cachedAttribute);
                if (values == null || values.isEmpty()) {
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

    /*
        Check if the attibute is an open street map built-in importer that identifies different categories and eventually
        add the attibutes and values to che cached map.
     */
    private boolean checkOSMBuiltIn(AttributeMatcher attributeMatcher) {
        for(BuiltInImporters bii: (BuiltInImporters.values())) {
            if (bii.getLabel().equals(attributeMatcher.label)) {
                for (String category: bii.getCategories().keySet()) {
                    cachedValueAttributes.put(
                            AttributeUtils.getByProviderAndLabel(attributeMatcher.provider, category),
                            bii.getCategories().get(category));
                }
                return true;
            }
        }
        return false;
    }
}
