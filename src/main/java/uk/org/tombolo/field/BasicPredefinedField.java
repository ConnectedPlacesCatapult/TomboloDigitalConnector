package uk.org.tombolo.field;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.execution.spec.DatasourceSpecification;
import uk.org.tombolo.execution.spec.FieldSpecification;
import uk.org.tombolo.execution.spec.SpecificationDeserializer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;

/**
 * A field that takes as input a specification (recipe) of a potentially complex field
 * and returns a value that is calculated according to the specification.
 */
public class BasicPredefinedField implements Field, PredefinedField {
    String label;
    String name;
    String recipe;
    Field field;
    List<DatasourceSpecification> datasourceSpecifications;

    // Path and postfixes for predefined field specifications
    // Could be made configurable at some point
    protected static final String fieldSpecPath = "predefined-fields/";
    protected static final String fieldSpecPostfix = "-field.json";
    protected static final String fieldDataPostfix = "-data.json";

    public BasicPredefinedField(String label, String name, String recipe){
        this.label = label;
        this.name = name;
        this.recipe = recipe;
    }

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
        JSONObject obj = new JSONObject();
        JSONObject fieldValue = field.jsonValueForSubject(subject);
        // Unwrap the fieldValue by getting based on the label, then rewrap with the label the user specified
        obj.put(label, fieldValue.get(field.getLabel()));
        return obj;
    }

    @Override
    public String getLabel() {
        return label;
    }

    private void initialize() {
        String fieldSpecificationFilename = fieldSpecPath+recipe+fieldSpecPostfix;
        URL fieldSpecificationFileURL = ClassLoader.getSystemResource(fieldSpecificationFilename);
        File fieldSpecificationFile = new File(fieldSpecificationFileURL.getFile());
        try {
            field = SpecificationDeserializer
                    .fromJsonFile(fieldSpecificationFile, FieldSpecification.class)
                    .toField();
        } catch (ClassNotFoundException e) {
            throw new Error("Field class not found", e);
        } catch (IOException e) {
            throw new Error("Could not read specification file", e);
        }

        String dataSpecificationFilename = fieldSpecPath+recipe+fieldDataPostfix;
        URL dataSpecificationFileURL = ClassLoader.getSystemResource(dataSpecificationFilename);
        File dataSpecificationFile = new File(dataSpecificationFileURL.getFile());
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            Type type = new TypeToken<List<DatasourceSpecification>>(){}.getType();
            datasourceSpecifications =  gson.fromJson(FileUtils.readFileToString(dataSpecificationFile), type);
        } catch (IOException e) {
            throw new Error("Could not read specification file", e);
        }
    }
}
