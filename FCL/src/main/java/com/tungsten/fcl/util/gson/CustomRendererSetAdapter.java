package com.tungsten.fcl.util.gson;

import static com.tungsten.fcl.util.AndroidUtils.getStringValue;

import com.mio.data.Renderer;

import java.io.File;
import java.util.*;

import com.google.gson.*;
import com.tungsten.fclcore.util.StringUtils;

import java.lang.reflect.Type;

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
            String boatStr = getStringValue(rendererJson, "boatEnv");
            String pojavStr = getStringValue(rendererJson, "pojavEnv");
            String minMCVer = getStringValue(rendererJson, "minMCVer");
            String maxMCVer = getStringValue(rendererJson, "maxMCVer");
            boolean success = StringUtils.allStringsValid(packageName, des, renderer);
            if(!success || (boatStr.trim().isEmpty() && pojavStr.trim().isEmpty())) continue;

            String[] renderEnv = renderer.split(":");
            if(!checkSoFiles(boatStr, pojavStr, renderer)) continue;

            Renderer rendererObj = new Renderer(
                    renderEnv[0].trim(), des.trim(), renderEnv[1].trim(), renderEnv[2].trim(), libDir,
                    List.of(boatStr.split(":")), List.of(pojavStr.split(":")), packageName.trim(),
                    minMCVer.trim(), maxMCVer.trim()
            );
            rendererSet.add(rendererObj);
        }

        return rendererSet;
    }

    private boolean checkSoFiles(String... envStrings) {
        if (envStrings == null || envStrings.length == 0) return false;

        return Arrays.stream(envStrings)
                .filter(Objects::nonNull)
                .flatMap(s -> Arrays.stream(s.split(":")))
                .flatMap(s -> Arrays.stream(s.split("[^A-Za-z0-9_\\-.]+")))
                .filter(name -> name.endsWith(".so"))
                .map(name -> new File(libDir, name))
                .allMatch(File::exists);
    }
}