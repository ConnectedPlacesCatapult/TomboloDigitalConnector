package uk.org.tombolo.recipe;

import com.google.gson.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;

public class RecipeDeserializer {
    public static <T> T fromJson(String jsonString, Class<T> returningClass) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(FieldRecipe.class, new FieldDeserializer());
        Gson gson = gsonBuilder.create();
        return gson.fromJson(jsonString, returningClass);
    }

    public static <T> T fromJsonFile(File jsonFile, Class<T> returningClass) throws IOException {
        return fromJson(FileUtils.readFileToString(jsonFile), returningClass);
    }

    public static <T> T fromJsonString(String json, Class<T> returningClass) throws IOException {
        return fromJson(json, returningClass);
    }

    private static class FieldDeserializer implements JsonDeserializer<FieldRecipe> {
        @Override
        public FieldRecipe deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = (JsonObject) json;
            String fieldClass = (String) jsonObject.remove("fieldClass").getAsString();
            return new FieldRecipe(fieldClass, jsonObject.toString());
        }
    }
}
