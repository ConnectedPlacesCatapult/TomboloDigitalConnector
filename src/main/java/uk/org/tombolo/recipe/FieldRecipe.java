package uk.org.tombolo.recipe;

import uk.org.tombolo.field.Field;

public class FieldRecipe {
    private String fieldClass;
    private String json;

    public FieldRecipe(String fieldClass, String json){
        this.fieldClass = fieldClass;
        this.json = json;
    }

    public Field toField() throws ClassNotFoundException {
        return (Field) RecipeDeserializer.fromJson(json, Class.forName(fieldClass));
    }
}
