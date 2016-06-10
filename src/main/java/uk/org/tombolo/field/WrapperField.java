package uk.org.tombolo.field;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.execution.spec.FieldSpecification;

import java.util.List;

public class WrapperField implements Field {
    private final List<FieldSpecification> fieldSpecification;
    private final String label;

    WrapperField(String label, List<FieldSpecification> fieldSpecification) {
        this.label = label;
        this.fieldSpecification = fieldSpecification;
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject) {
        JSONObject obj = new JSONObject();
        JSONObject inner = new JSONObject();
        for (FieldSpecification fieldSpec : fieldSpecification) {
            try {
                inner.putAll(fieldSpec.toField().jsonValueForSubject(subject));
            } catch (ClassNotFoundException e) {
                throw new Error("Field not valid.");
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
}
