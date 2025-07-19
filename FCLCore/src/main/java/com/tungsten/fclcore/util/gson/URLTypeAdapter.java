package com.tungsten.fclcore.util.gson;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.net.URL;

public class URLTypeAdapter implements JsonSerializer<URL>, JsonDeserializer<URL> {
    @Override
    public JsonElement serialize(URL src, Type typeOfSrc, JsonSerializationContext context) {
        return src == null ? null : new JsonPrimitive(src.toString());
    }

    @Override
    public URL deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if(json.isJsonNull()) return null;

        String urlString = json.getAsString();
        if(urlString == null || urlString.trim().isEmpty()) return null;

        try {
            return new URL(urlString);
        }catch(Exception e) {
            return null;
        }
    }
}