package com.tungsten.fcl.util.gson;

import com.google.gson.*;
import com.mio.data.Renderer;
import com.mio.manager.RendererManager;
import com.tungsten.fcl.util.AndroidUtils;

import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class RuleRendererSetAdapter implements JsonSerializer<LinkedHashSet<Renderer>>, JsonDeserializer<LinkedHashSet<Renderer>> {

    @Override
    public JsonElement serialize(LinkedHashSet<Renderer> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray array = new JsonArray();

        if(src != null) {
            src.stream()
                .map(Renderer::getId)
                .forEach(array::add);
        }

        return array;
    }

    @Override
    public LinkedHashSet<Renderer> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if(json == null || !json.isJsonArray()) return new LinkedHashSet<>();

        return StreamSupport.stream(json.getAsJsonArray().spliterator(), false)
                .map(this::parseElement)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Renderer parseElement(JsonElement element) {
        if(element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return RendererManager.getRenderer(element.getAsString());
        }else if(element.isJsonObject()) return parseRendererObject(element.getAsJsonObject());

        return null;
    }

    private Renderer parseRendererObject(JsonObject obj) {
        return Optional.ofNullable(obj)
                .filter(o -> o.has("packageName") && o.has("name"))
                .filter(o -> isValidStringElement(o.get("packageName")) && isValidStringElement(o.get("name")))
                .filter(o -> {
                    String name = o.get("name").getAsString();
                    String packageName = o.get("packageName").getAsString();
                    return name != null && AndroidUtils.isRegexMatch(packageName, "^[a-zA-Z][a-zA-Z0-9_]*(\\.([a-zA-Z][a-zA-Z0-9_]*))+$");
                })
                .map(o -> new Renderer(
                        o.get("name").getAsString(), "", "", "", "", null,
                        null, o.get("packageName").getAsString(), "", ""
                ))
                .orElse(null);
    }

    private boolean isValidStringElement(JsonElement element) {
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isString();
    }
}