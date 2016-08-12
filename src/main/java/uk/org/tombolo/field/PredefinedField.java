package uk.org.tombolo.field;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.execution.spec.DatasourceSpecification;
import uk.org.tombolo.execution.spec.FieldSpecification;
import uk.org.tombolo.execution.spec.SpecificationDeserializer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 */
public class PredefinedField implements Field, SelfcontainedField {
    String label;
    Field field;
    List<DatasourceSpecification> datasourceSpecifications;

    Logger log = LoggerFactory.getLogger(PredefinedField.class);

    @Override
    public List<DatasourceSpecification> getDatasourceSpecifications() {
        if (field == null)
            initialize();
        return datasourceSpecifications;
    }

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
        String fieldSpecificationFilename = "predefined-fields/"+label+"-field.json";
        URL fieldSpecificationFileURL = ClassLoader.getSystemResource(fieldSpecificationFilename);
        File fieldSpecificationFile = new File(fieldSpecificationFileURL.getFile());
        try {
            field = SpecificationDeserializer
                    .fromJsonFile(fieldSpecificationFile, FieldSpecification.class)
                    .toField();
        } catch (ClassNotFoundException e) {
            throw new Error("Field class not found", e);
        } catch (IOException e) {
            throw new Error("Could not read specificaiton file", e);
        }

        String dataSpecificationFilename = "predefined-fields/"+label+"-data.json";
        URL dataSpecificationFileURL = ClassLoader.getSystemResource(dataSpecificationFilename);
        File dataSpecificationFile = new File(dataSpecificationFileURL.getFile());
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            Type type = new TypeToken<List<DatasourceSpecification>>(){}.getType();
            datasourceSpecifications =  gson.fromJson(FileUtils.readFileToString(dataSpecificationFile), type);
            for (Object o : datasourceSpecifications){
                log.info(o.getClass().getName());
            }
        } catch (IOException e) {
            throw new Error("Could not read specificaiton file", e);
        }
    }
}
