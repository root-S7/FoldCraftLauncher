package com.tungsten.fcl.util.gson;

import static com.mio.JavaManager.getJavaList;
import static com.tungsten.fcl.util.AndroidUtils.isRegexMatch;

import static java.util.stream.StreamSupport.stream;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tungsten.fclcore.game.JavaVersion;

import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public class RuleJavaSetAdapter implements JsonSerializer<LinkedHashSet<JavaVersion>>, JsonDeserializer<LinkedHashSet<JavaVersion>> {
    private static final String VALID_FOLDER_NAME_PATTERN = "^[^<>:\"/\\\\|?*\\x00-\\x1f]{1,95}[^<>:\"/\\\\|?*\\x00-\\x1f .]$|^[^<>:\"/\\\\|?*\\x00-\\x1f .]$";

    @Override
    public LinkedHashSet<JavaVersion> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonArray()) {
            return new LinkedHashSet<>();
        }

        JsonArray jsonArray = json.getAsJsonArray();
        List<JavaVersion> javaList = getJavaList();

        return stream(jsonArray.spliterator(), false)
                .filter(JsonElement::isJsonPrimitive)
                .map(JsonElement::getAsString)
                .filter(name -> isRegexMatch(name, VALID_FOLDER_NAME_PATTERN))
                .map(name -> findOrCreateJavaVersion(name, javaList))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public JsonElement serialize(LinkedHashSet<JavaVersion> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray jsonArray = new JsonArray();
        src.stream()
                .map(JavaVersion::getName)
                .forEach(jsonArray::add);

        return jsonArray;
    }

    /**
     * 查找或创建JavaVersion对象（不区分大小写比对）
     */
    private JavaVersion findOrCreateJavaVersion(String name, List<JavaVersion> javaList) {
        return javaList.stream()
                .filter(java -> java.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> new JavaVersion(false, "unknown", name));
    }
}