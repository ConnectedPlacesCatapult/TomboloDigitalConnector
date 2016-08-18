package uk.org.tombolo.field;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.execution.spec.FieldSpecification;

import java.util.ArrayList;
import java.util.List;

/**
 * WrapperField.java
 * A field that wraps subfields in a JSON object.
 *
 * Takes a fieldSpecification exactly like the root fieldSpecification does.
 * Can be nested.
 */
public class WrapperField implements Field, ParentField {
    private static Logger log = LoggerFactory.getLogger(WrapperField.class);
    private final List<FieldSpecification> fieldSpecification;
    private final String label;
    private ArrayList<Field> fields;

    WrapperField(String label, List<FieldSpecification> fieldSpecification) {
        this.label = label;
        this.fieldSpecification = fieldSpecification;
    }

    public void initialize() {
        this.fields = new ArrayList<>();
        for (FieldSpecification fieldSpec : fieldSpecification) {
            try {
                fields.add(fieldSpec.toField());
            } catch (ClassNotFoundException e) {
                throw new Error("Field not valid");
            }
        }
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject) {
        if (null == fields) { initialize(); }
        JSONObject obj = new JSONObject();
        JSONObject inner = new JSONObject();
        for (Field field : fields) {
            try {
                inner.putAll(field.jsonValueForSubject(subject));
            } catch (IncomputableFieldException e) {
                log.warn("Could not compute Field {} for Subject {}, reason: {}", field.getLabel(), subject.getLabel(), e.getMessage());
            }
        }
        obj.put(label, inner);
        return obj;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getHumanReadableName() {
        return label;
    }

    @Override
    public List<Field> getChildFields() {
        if (null == fields) { initialize(); }
        return fields;
    }
}
