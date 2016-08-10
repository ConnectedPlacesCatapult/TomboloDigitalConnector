package uk.org.tombolo.field;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.execution.spec.DatasourceSpecification;
import uk.org.tombolo.execution.spec.FieldSpecification;
import uk.org.tombolo.execution.spec.SpecificationDeserializer;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *
 */
public class PredefinedField implements Field {
    Field field;
    List<DatasourceSpecification> datasourceSpecificationList;

    public PredefinedField(String label) throws IOException, ClassNotFoundException {
        File fieldSpecificaitonFile = new File(ClassLoader.getSystemResource("/predefined-fields/"+label+"-field.json").getFile());
        field = SpecificationDeserializer
                .fromJsonFile(fieldSpecificaitonFile, FieldSpecification.class)
                .toField();
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject) throws IncomputableFieldException {
        return field.jsonValueForSubject(subject);
    }

    @Override
    public String getLabel() {
        return field.getLabel();
    }

    @Override
    public String getHumanReadableName() {
        return field.getHumanReadableName();
    }
}
