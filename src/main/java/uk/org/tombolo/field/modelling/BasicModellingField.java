package uk.org.tombolo.field.modelling;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.recipe.DatasourceRecipe;
import uk.org.tombolo.recipe.FieldRecipe;
import uk.org.tombolo.recipe.RecipeDeserializer;
import uk.org.tombolo.field.AbstractField;
import uk.org.tombolo.field.Field;
import uk.org.tombolo.field.IncomputableFieldException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;

/**
 * A field that takes as input a specification (recipe) of a potentially complex field or model
 * and returns a value that is calculated according to the specification.
 */
public class BasicModellingField extends AbstractField implements Field, ModellingField {
    String recipe;
    Field field;
    List<DatasourceRecipe> datasourceRecipes;

    // Path and postfixes for predefined field specifications
    // Could be made configurable at some point
    protected static final String fieldSpecPath = "modelling-fields/";
    protected static final String fieldSpecPostfix = "-field.json";
    protected static final String fieldDataPostfix = "-data.json";

    public BasicModellingField(String label, String recipe){
        super(label);
        this.recipe = recipe;
    }

    @Override
    public List<DatasourceRecipe> getDatasourceRecipes() {
        if (field == null)
            initialize();
        return datasourceRecipes;
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

    protected void initialize() {
        String fieldSpecificationFilename = fieldSpecPath+recipe+fieldSpecPostfix;
        URL fieldSpecificationFileURL = ClassLoader.getSystemResource(fieldSpecificationFilename);
        File fieldSpecificationFile = new File(fieldSpecificationFileURL.getFile());
        try {
            field = RecipeDeserializer
                    .fromJsonFile(fieldSpecificationFile, FieldRecipe.class)
                    .toField();
            field.setFieldCache(fieldCache);
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
            Type type = new TypeToken<List<DatasourceRecipe>>(){}.getType();
            datasourceRecipes =  gson.fromJson(FileUtils.readFileToString(dataSpecificationFile), type);
        } catch (IOException e) {
            throw new Error("Could not read specification file", e);
        }
    }
}
