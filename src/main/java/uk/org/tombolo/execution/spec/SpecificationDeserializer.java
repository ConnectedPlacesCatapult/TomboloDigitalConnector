package uk.org.tombolo.execution.spec;

import com.google.gson.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;

public class SpecificationDeserializer {
    public static <T> T fromJson(String jsonString, Class<T> returningClass) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(FieldSpecification.class, new FieldSpecificationDeserializer());
        Gson gson = gsonBuilder.create();
        return gson.fromJson(jsonString, returningClass);
    }

    public static <T> T fromJsonFile(File jsonFile, Class<T> returningClass) throws IOException {
        return fromJson(FileUtils.readFileToString(jsonFile), returningClass);
    }

    private static class FieldSpecificationDeserializer implements JsonDeserializer<FieldSpecification> {
        @Override
        public FieldSpecification deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = (JsonObject) json;
            String fieldClass = (String) jsonObject.remove("fieldClass").getAsString();
            return new FieldSpecification(fieldClass, jsonObject.toString());
        }
    }
}
