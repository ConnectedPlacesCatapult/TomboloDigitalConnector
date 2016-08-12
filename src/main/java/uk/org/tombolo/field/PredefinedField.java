package uk.org.tombolo.field;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.execution.spec.DatasourceSpecification;
import uk.org.tombolo.execution.spec.FieldSpecification;
import uk.org.tombolo.execution.spec.SpecificationDeserializer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 *
 */
public class PredefinedField implements Field {
    String fieldSpecification;
    Field field;
    List<DatasourceSpecification> datasourceSpecificationList;

    Logger log = LoggerFactory.getLogger(PredefinedField.class);

    @Override
    public JSONObject jsonValueForSubject(Subject subject) throws IncomputableFieldException {
        if (field == null)
            initialize();
        return field.jsonValueForSubject(subject);
    }

    @Override
    public String getLabel() {
        if (field == null)
            initialize();
        return field.getLabel();
    }

    @Override
    public String getHumanReadableName() {
        if (field == null)
            initialize();
        return field.getHumanReadableName();
    }

    private void initialize() {
        String fieldSpecificationFilename = "predefined-fields/"+fieldSpecification+"-field.json";
        URL fieldSpecificationFileURL = ClassLoader.getSystemResource(fieldSpecificationFilename);
        File fieldSpecificaitonFile = new File(fieldSpecificationFileURL.getFile());
        try {
            field = SpecificationDeserializer
                    .fromJsonFile(fieldSpecificaitonFile, FieldSpecification.class)
                    .toField();
        } catch (ClassNotFoundException e) {
            throw new Error("Field class not found", e);
        } catch (IOException e) {
            throw new Error("Could not read specificaiton file", e);
        }
    }
}
