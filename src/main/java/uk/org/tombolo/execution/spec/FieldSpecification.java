package uk.org.tombolo.execution.spec;

import com.google.gson.*;
import uk.org.tombolo.field.Field;

import java.lang.reflect.Type;

public class FieldSpecification {
    private String fieldClass;
    private String json;

    public FieldSpecification(String fieldClass, String json){
        this.fieldClass = fieldClass;
        this.json = json;
    }

    public Field toField() throws ClassNotFoundException {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(FieldSpecification.class, new FieldSpecification.FieldSpecificationDeserializer());
        Gson gson = gsonBuilder.create();
        return (Field) gson.fromJson(json, Class.forName(fieldClass));
    }

    public static class FieldSpecificationDeserializer implements JsonDeserializer<FieldSpecification> {
        @Override
        public FieldSpecification deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = (JsonObject) json;
            String fieldClass = (String) jsonObject.remove("fieldClass").getAsString();
            return new FieldSpecification(fieldClass, jsonObject.toString());
        }
    }
}
