package uk.org.tombolo.field.modelling;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.field.AbstractField;
import uk.org.tombolo.field.Field;
import uk.org.tombolo.field.IncomputableFieldException;
import uk.org.tombolo.field.ParentField;
import uk.org.tombolo.recipe.DatasourceRecipe;
import uk.org.tombolo.recipe.FieldRecipe;
import uk.org.tombolo.recipe.RecipeDeserializer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * A field that takes as input a specification (recipe) of a potentially complex field or model
 * and returns a value that is calculated according to the specification.
 */
public class BasicModellingField extends AbstractField implements ModellingField, ParentField {
    // Variables that can be passed by the user in recipes
    String recipe;
    List<DatasourceRecipe> datasources; // This is an optional field that can be used to override the datasources

    // Variables that are updated by the class
    Field field;

    // Path and postfixes for predefined field specifications
    // Could be made configurable at some point
    protected static final String fieldSpecPath = "modelling-fields/";
    protected static final String fieldSpecPostfix = "-field.json";
    protected static final String fieldDataPostfix = "-data.json";

    public BasicModellingField(String label, String recipe, List<DatasourceRecipe> datasources){
        super(label);
        this.recipe = recipe;
        this.datasources = datasources;
    }

    @Override
    public List<DatasourceRecipe> getDatasources() {
        if (datasources == null)
            initializeDatasources();
        return datasources;
    }

    @Override
    public String valueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        if (field == null) initialize();
        return field.jsonValueForSubject(subject, timeStamp).toJSONString();
    }

    public void initialize() {
        // Initialise field
        String fieldFilename = fieldSpecPath+recipe+fieldSpecPostfix;
        URL fieldFileURL = ClassLoader.getSystemResource(fieldFilename);
        if (fieldFileURL == null){
            throw new Error("Model Recipe not found: "+fieldFilename);
        }
        File fieldFile = new File(fieldFileURL.getFile());
        try {
            field = RecipeDeserializer
                    .fromJsonFile(fieldFile, FieldRecipe.class)
                    .toField();
            field.setFieldCache(fieldCache);
        } catch (ClassNotFoundException e) {
            throw new Error("Field class not found", e);
        } catch (IOException e) {
            throw new Error("Could not read specification file", e);
        }

        // Initialise data-sources
        initializeDatasources();
    }

    private void initializeDatasources(){
        if (datasources == null) {
            String dataSpecificationFilename = fieldSpecPath + recipe + fieldDataPostfix;
            URL dataSpecificationFileURL = ClassLoader.getSystemResource(dataSpecificationFilename);
            File dataSpecificationFile = new File(dataSpecificationFileURL.getFile());
            try {
                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.create();
                Type type = new TypeToken<List<DatasourceRecipe>>() {
                }.getType();
                datasources = gson.fromJson(FileUtils.readFileToString(dataSpecificationFile), type);
            } catch (IOException e) {
                throw new Error("Could not read specification file", e);
            }
        }
    }

    @Override
    public List<Field> getChildFields() {
        if (field == null)
            initialize();
        return Collections.singletonList(field);
    }
}
