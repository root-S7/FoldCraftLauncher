package com.tungsten.fcl.util.gson;

import static com.tungsten.fcl.util.AndroidUtils.getStringValue;

import com.mio.data.Renderer;

import java.io.File;
import java.util.*;

import com.google.gson.*;
import com.tungsten.fclcore.util.StringUtils;

import java.lang.reflect.Type;
import java.util.stream.*;

public class CustomRendererSetAdapter implements JsonSerializer<Set<Renderer>>, JsonDeserializer<Set<Renderer>> {
    private final String libDir;

    public CustomRendererSetAdapter(String libDir) {
        this.libDir = libDir;
    }

    @Override
    public JsonElement serialize(Set<Renderer> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray array = new JsonArray();
        for (Renderer renderer : src) {
            array.add(renderer.getDes());
        }
        return array;
    }

    @Override
    public Set<Renderer> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        Set<Renderer> rendererSet = new HashSet<>();
        if(!json.isJsonObject()) return rendererSet;

        JsonObject jsonObject = json.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String packageName = entry.getKey();
            JsonElement rendererElement = entry.getValue();

            if(!rendererElement.isJsonObject()) continue;
            JsonObject rendererJson = rendererElement.getAsJsonObject();
            String des = getStringValue(rendererJson, "des");
            String renderer = getStringValue(rendererJson, "renderer");
            String boatString = getStringValue(rendererJson, "boatEnv");
            String pojavString = getStringValue(rendererJson, "pojavEnv");
            String minMCVer = getStringValue(rendererJson, "minMCVer");
            String maxMCVer = getStringValue(rendererJson, "maxMCVer");
            boolean success = StringUtils.allStringsValid(packageName, des, renderer);
            if(!success || (boatString.trim().isEmpty() && pojavString.trim().isEmpty())) continue;

            String[] boatEnv = boatString.split(":");
            String[] pojavEnv = pojavString.split(":");
            String[] renderEnv = renderer.split(":");
            if(!checkSoFiles(Stream.of(renderEnv, boatEnv, pojavEnv)
                    .flatMap(Arrays::stream)
                    .toArray(String[]::new))) continue;

            Renderer rendererObj = new Renderer(
                    renderEnv[0].trim(), des.trim(), renderEnv[1].trim(), renderEnv[2].trim(), libDir,
                    List.of(boatEnv), List.of(pojavEnv), packageName.trim(), minMCVer.trim(), maxMCVer.trim()
            );
            rendererSet.add(rendererObj);
        }

        return rendererSet;
    }

    private boolean checkSoFiles(String... items) {
        return Arrays.stream(items)
                .filter(s -> s != null && s.endsWith(".so"))
                .map(s -> new File(libDir, s))
                .allMatch(File::exists);
    }
}